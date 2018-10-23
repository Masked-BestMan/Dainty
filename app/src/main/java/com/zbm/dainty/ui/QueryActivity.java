package com.zbm.dainty.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zbm.dainty.util.DaintyDBHelper;
import com.zbm.dainty.util.MyUtil;
import com.zbm.dainty.bean.QueryItemBean;
import com.zbm.dainty.adapter.QueryListAdapter;
import com.zbm.dainty.R;
import com.zbm.dainty.widget.QueryListView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Zbm阿铭 on 2018/1/24.
 */

public class QueryActivity extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.query_engine)
    ImageView queryEngine;
    @BindView(R.id.edit_text)
    EditText editText;
    @BindView(R.id.query_button)
    TextView queryButton;
    @BindView(R.id.voice_recognition)
    ImageView voiceRecognition;
    @BindView(R.id.clear_text)
    ImageView clearButton;
    @BindView(R.id.query_history_list)
    QueryListView listView;
    @BindView(R.id.query_bar_theme)
    View queryBarTheme;

    private InputMethodManager mInputMethodManager;
    private boolean isURL = false;
    private ArrayList<QueryItemBean> data;
    private QueryListAdapter adapter;
    private PopupWindow deleteWindow;
    private int selectedItem;
    private PopupWindow myPopupWindow;
    private int currentEngine = 1;

    private static final int BaiDu = 1;
    private static final int S360 = 2;
    private static final int BiYing = 3;
    private static final int REQUEST_RECOGNIZE = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        data = new ArrayList<>();
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        queryBarTheme.setBackgroundColor(Color.parseColor(preferences.getString("theme_color", "#474747")));
        adapter = new QueryListAdapter(this, data);
        adapter.setOnFillingClickListener(new QueryListAdapter.OnFillingClickListener() {
            @Override
            public void onFilling(String text) {
                editText.setText(text);
                editText.setSelection(text.length());
            }
        });
        adapter.setOnHeadClickListener(new QueryListAdapter.OnHeadClickListener() {
            @Override
            public void onClick() {
                showNormalDialog();
            }
        });
        listView.setAdapter(adapter);
        queryEngine.setOnClickListener(this);
        voiceRecognition.setOnClickListener(this);
        queryButton.setOnClickListener(this);
        clearButton.setOnClickListener(this);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!TextUtils.isEmpty(editable)) {
                    queryButton.setText(R.string.query_button_title1);
                    voiceRecognition.setVisibility(View.INVISIBLE);
                    clearButton.setVisibility(View.VISIBLE);
                } else {
                    switch (currentEngine) {
                        case BaiDu:
                            queryEngine.setImageResource(R.drawable.baidu_icon);
                            break;
                        case S360:
                            queryEngine.setImageResource(R.drawable.s360_icon);
                            break;
                        default:
                            queryEngine.setImageResource(R.drawable.biying_icon);
                    }
                    queryEngine.setEnabled(true);
                    queryButton.setText(R.string.query_button_title2);
                    voiceRecognition.setVisibility(View.VISIBLE);
                    clearButton.setVisibility(View.INVISIBLE);
                    searchAll("select * from " + DaintyDBHelper.QTB_NAME + " order by queryTIME desc");
                    return;
                }
                //判断是否输入正确URL
                if (Patterns.WEB_URL.matcher(editable).matches() || URLUtil.isValidUrl(String.valueOf(editable))) {
                    isURL = true;
                    queryEngine.setImageResource(R.drawable.enter_url);
                    queryEngine.setEnabled(false);
                } else {
                    isURL = false;
                    switch (currentEngine) {
                        case BaiDu:
                            queryEngine.setImageResource(R.drawable.baidu_icon);
                            break;
                        case S360:
                            queryEngine.setImageResource(R.drawable.s360_icon);
                            break;
                        default:
                            queryEngine.setImageResource(R.drawable.biying_icon);
                    }
                    queryEngine.setEnabled(true);
                }
                searchAll("select * from " + DaintyDBHelper.QTB_NAME + " where queryNAME like '%" + editable.toString() + "%' order by queryTIME desc");
            }
        });
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_ENTER) {
                    executeSearch(queryButton);
                }
                return false;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (mInputMethodManager.isActive())
                    mInputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                QueryItemBean queryItemBean = data.get(i-1);
                insertOrUpdateTable(queryItemBean.getQueryNAME());
                String value = queryItemBean.getQueryNAME();
                Log.d("aaa","value:"+value);
                if (queryItemBean.getQueryTYPE().equals("url")) {
                    if (!value.contains("http://") && !value.contains("https://")) {
                        value = "http://" + value;
                    }
                } else {
                    switch (currentEngine) {
                        case BaiDu:
                            value = "https://www.baidu.com/s?wd=" + value;
                            break;
                        case S360:
                            value = "https://www.so.com/s?q=" + value;
                            break;
                        default:
                            value = "http://cn.bing.com/search?q=" + value;
                    }
                }
                Intent intent = new Intent();
                intent.putExtra("currentUri", value);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                selectedItem = i;
                int[] positions = new int[2];
                view.getLocationOnScreen(positions);
                deleteWindow.showAtLocation(view, Gravity.TOP | Gravity.END, 50, positions[1] + MyUtil.dip2px(QueryActivity.this, 60));
                return true;
            }
        });
        listView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_MOVE && mInputMethodManager.isActive())
                    mInputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                return false;
            }
        });
        @SuppressLint("InflateParams")
        View contentView = LayoutInflater.from(this).inflate(R.layout.query_item_delete_window, null);
        Button deleteButton = contentView.findViewById(R.id.deleteButton);
        deleteButton.setText("删除该条记录");
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteWindow.dismiss();
                DaintyDBHelper.getDaintyDBHelper(QueryActivity.this).deleteTableItem(DaintyDBHelper.QTB_NAME,"where queryNAME='"+data.get(selectedItem-1).getQueryNAME()+"'");
                data.remove(selectedItem-1);
                adapter.notifyDataSetChanged();
            }
        });
        deleteWindow = new PopupWindow(contentView, MyUtil.dip2px(this,120),
                ViewGroup.LayoutParams.WRAP_CONTENT);
        deleteWindow.setFocusable(true);
        deleteWindow.setOutsideTouchable(true);
        deleteWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        searchAll("select * from " + DaintyDBHelper.QTB_NAME + " order by queryTIME desc");
        initPopupWindow();
    }

    @Override
    public void onClick(View view) {
        int object = view.getId();
        switch (object) {
            case R.id.query_engine:
                myPopupWindow.showAsDropDown(queryBarTheme);
                break;
            case R.id.voice_recognition:
                startActivityForResult(new Intent(this, RecognizeActivity.class), REQUEST_RECOGNIZE);
                break;
            case R.id.query_button:
                executeSearch(view);
                break;
            case R.id.clear_text:
                editText.setText("");
                break;
        }
    }

    private void executeSearch(View view) {
        if (mInputMethodManager.isActive())
            mInputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        if (((TextView) view).getText().equals("搜索")) {
            insertOrUpdateTable(String.valueOf(editText.getText()));
            String value;
            if (isURL) {
                value = String.valueOf(editText.getText());
                if (!value.contains("http://") && !value.contains("https://")) {
                    value = "http://" + value;
                }
            } else {
                switch (currentEngine) {
                    case BaiDu:
                        value = "https://www.baidu.com/s?wd=" + editText.getText();
                        break;
                    case S360:
                        value = "https://www.so.com/s?q=" + editText.getText();
                        break;
                    default:
                        value = "http://cn.bing.com/search?q=" + editText.getText();
                }
            }
            Intent intent = new Intent();
            intent.putExtra("currentUri", value);
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    private void searchAll(final String sql) {
        DaintyDBHelper.getDaintyDBHelper(this).searchQueryTable(sql, new DaintyDBHelper.OnSearchQueryTableListener() {
            @Override
            public void onResult(ArrayList<QueryItemBean> mQueryData) {
                data.clear();
                data.addAll(mQueryData);
                adapter.notifyDataSetChanged();
            }
        });

    }

    private void insertOrUpdateTable(final String text) {
        DaintyDBHelper.getDaintyDBHelper(this).updateQueryTable(text,isURL);
    }


    private void showNormalDialog() {
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        MyUtil.createDialog(this,"删除提示","确认清空输入记录？","确定",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DaintyDBHelper.getDaintyDBHelper(QueryActivity.this).deleteTableItem(DaintyDBHelper.QTB_NAME,null);
                data.clear();
                adapter.notifyDataSetChanged();
            }
        },null);
    }

    private void initPopupWindow() {
        @SuppressLint("InflateParams")
        View popupLayout = LayoutInflater.from(this).inflate(R.layout.popup_query, null);
        TextView duEngine = popupLayout.findViewById(R.id.baidu_engine);
        duEngine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryEngine.setImageResource(R.drawable.baidu_icon);
                currentEngine = BaiDu;
                myPopupWindow.dismiss();
            }
        });
        TextView sEngine = popupLayout.findViewById(R.id.s360_engine);
        sEngine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryEngine.setImageResource(R.drawable.s360_icon);
                currentEngine = S360;
                myPopupWindow.dismiss();
            }
        });
        TextView yingEngine = popupLayout.findViewById(R.id.biying_engine);
        yingEngine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryEngine.setImageResource(R.drawable.biying_icon);
                currentEngine = BiYing;
                myPopupWindow.dismiss();
            }
        });
        myPopupWindow = new PopupWindow(popupLayout, WindowManager.LayoutParams.MATCH_PARENT,
                MyUtil.dip2px(this, 100));
        myPopupWindow.setFocusable(true);
        myPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        myPopupWindow.setAnimationStyle(R.style.query_popup_animation);
        myPopupWindow.setOutsideTouchable(true);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RECOGNIZE && resultCode == Activity.RESULT_OK) {
            String word = data.getStringExtra("result");
            editText.setText(word);
            executeSearch(queryButton);
        }
    }
}
