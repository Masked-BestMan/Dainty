package com.zbm.dainty.ui;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zbm.dainty.util.HttpUtil;
import com.zbm.dainty.util.MyUtil;
import com.zbm.dainty.R;
import com.zbm.dainty.widget.SwipeBackActivity;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by Zbm阿铭 on 2017/5/10.
 */

public class LoginActivity extends SwipeBackActivity {
    @BindView(R.id.account)
    EditText account;
    @BindView(R.id.password)
    EditText password;
    @BindView(R.id.login)
    Button login;
    @BindView(R.id.register)
    TextView registerButton;
    @BindView(R.id.forget_password)
    TextView forgetButton;
    @BindView(R.id.login_back)
    Button close;
    @BindView(R.id.login_bar_theme)
    View loginBarTheme;

    private final int SUCCESS = 1;
    private final int FAIL = 0;
    private final int NOT_NETWORK = -1;
    private Bitmap touxiang;
    private InputMethodManager inputMethodManager;
    private Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    touxiang = HttpUtil.getD();
                    Toast.makeText(LoginActivity.this, "登陆成功！", Toast.LENGTH_SHORT).show();
                    break;
                case FAIL:
                    Toast.makeText(LoginActivity.this, "账号或密码有误，请重试！", Toast.LENGTH_SHORT).show();
                    break;
                case NOT_NETWORK:
                    Toast.makeText(LoginActivity.this, "请检查网络！", Toast.LENGTH_SHORT).show();
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        ButterKnife.bind(this);
        inputMethodManager= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("touxiang", touxiang);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (account.getText().toString().equals("")||password.getText().toString().equals("")){
                    Toast.makeText(LoginActivity.this, "账户或密码不能为空！", Toast.LENGTH_SHORT).show();
                    return;
                }
                HttpUtil.checkLogin(account.getText().toString(), password.getText().toString(), new HttpUtil.HttpCallbackListener() {
                    @Override
                    public void onFinish(String response) {
                        switch (response) {
                            case "1":
                                mHandler.sendEmptyMessage(SUCCESS);
                                break;
                            case "0":
                                mHandler.sendEmptyMessage(FAIL);
                                break;
                            default:
                                mHandler.sendEmptyMessage(NOT_NETWORK);
                                break;
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        mHandler.sendEmptyMessage(NOT_NETWORK);
                    }
                });

            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        forgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);
        loginBarTheme.setBackgroundColor(Color.parseColor(preferences.getString("theme_color","#474747")));
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("touxiang", touxiang);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (MyUtil.isSoftInputMethodShowing(this)) {
            inputMethodManager.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}