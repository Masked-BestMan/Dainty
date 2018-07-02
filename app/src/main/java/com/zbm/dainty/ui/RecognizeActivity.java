package com.zbm.dainty.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.zbm.dainty.R;
import com.zbm.dainty.widget.RecordView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by zbm阿铭 on 2018/2/7.
 */

public class RecognizeActivity extends AppCompatActivity implements EventListener {
    @BindView(R.id.close_window)
    Button closeWindow;
    @BindView(R.id.recognizeResult)
    TextView recognizeResult;
    @BindView(R.id.startRecognize)
    TextView startRecognize;
    @BindView(R.id.recordView)
    RecordView recordView;
    private EventManager asr;
    private boolean canParse=false,canExit=true;
    private String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize);
        ButterKnife.bind(this);

        asr = EventManagerFactory.create(this, "asr");
        asr.registerListener(this); //  EventListener 中 onEvent方法
        start();
        startRecognize.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startRecognize.setVisibility(View.INVISIBLE);
                recordView.setVisibility(View.VISIBLE);
                start();
            }
        });
        closeWindow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
            initPermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
    }

    //   EventListener  回调方法
    @Override
    public void onEvent(String name, String params, byte[] data, int offset, int length) {
        switch (name){
            case SpeechConstant.CALLBACK_EVENT_ASR_READY:
                recognizeResult.setText("倾听中...");
                break;
            case SpeechConstant.CALLBACK_EVENT_ASR_BEGIN:
                recognizeResult.setText("解析中...");
                break;

            case SpeechConstant.CALLBACK_EVENT_ASR_END:
                canParse=true;
                break;

            case SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL:
                if (canParse){
                    try {
                        JSONObject object=new JSONObject(params);
                        result=object.getString("results_recognition");
                        result=result.substring(2,result.length()-2);
                        recognizeResult.setText(result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case SpeechConstant.CALLBACK_EVENT_ASR_FINISH:
                canParse=false;
                canExit=false;
                try {
                    JSONObject object=new JSONObject(params);
                    int errorCode=object.getInt("error");
                    if(errorCode==0){
                        canExit=true;
                    }else if (errorCode==7||errorCode==3){
                        recognizeResult.setText("没有听清，请重讲");
                    }else if (errorCode==2){
                        recognizeResult.setText("无法连接网络\n请检查网络设置");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
            case SpeechConstant.CALLBACK_EVENT_ASR_EXIT:
                if (canExit){
                    Intent intent = new Intent();
                    intent.putExtra("result", result);
                    setResult(RESULT_OK, intent);
                    finish();
                }else {
                    startRecognize.setVisibility(View.VISIBLE);
                    recordView.setVisibility(View.INVISIBLE);
                }
                break;
            case SpeechConstant.CALLBACK_EVENT_ASR_VOLUME:

                try {
                    JSONObject object = new JSONObject(params);
                    recordView.setVolume(object.getInt("volume-percent"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO
        };

        ArrayList<String> toApplyList = new ArrayList<>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.

            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            boolean isAllGranted = true;

            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (!isAllGranted) {
                // 如果用户拒绝授权，则弹出对话框让用户自行设置
                AlertDialog.Builder builder = new AlertDialog.Builder(RecognizeActivity.this);
                builder.setTitle("警告");
                builder.setMessage("当前应用缺少必要权限，请点击“设置”开启权限或点击“取消”关闭应用。");
                builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setData(Uri.fromParts("package", RecognizeActivity.this.getPackageName(), null));
                        RecognizeActivity.this.startActivity(intent);
                    }
                }).
                        setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                RecognizeActivity.this.setResult(RESULT_CANCELED);
                                RecognizeActivity.this.finish();
                            }
                        });

                AlertDialog dialog = builder.show();
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
            }
        }
    }

    private void start() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, true);  //需要语音音量数据回调，开启后有CALLBACK_EVENT_ASR_VOLUME事件回调
        String json = new JSONObject(params).toString(); // 这里可以替换成你需要测试的json
        asr.send(SpeechConstant.ASR_START, json, null, 0, 0);
    }

}
