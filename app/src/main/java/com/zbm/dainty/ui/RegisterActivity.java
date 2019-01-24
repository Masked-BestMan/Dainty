package com.zbm.dainty.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zbm.dainty.R;
import com.zbm.dainty.util.HttpUtil;
import com.zbm.dainty.util.MyUtil;
import com.zbm.dainty.widget.LoadingDialog;
import com.zbm.dainty.widget.SwipeBackActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class RegisterActivity extends SwipeBackActivity {
    @BindView(R.id.register_back)
    Button registerBack;
    @BindView(R.id.title)
    TextView tvTitle;
    @BindView(R.id.account)
    EditText etAccount;
    @BindView(R.id.et_new_password)
    EditText etNewPassword;
    @BindView(R.id.et_old_password)
    EditText etOldPassword;
    @BindView(R.id.btn_register)
    Button btnRegister;
    @BindView(R.id.register_bar_theme)
    View registerBarTheme;

    private InputMethodManager inputMethodManager;
    private CompositeDisposable disposable;
    private LoadingDialog dialog;
    private int type;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        disposable=new CompositeDisposable();
        inputMethodManager= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        /*
         * type=0 注册 type=1 修改密码
         */
        type=getIntent().getIntExtra("type",0);
        if (type==0){
            etOldPassword.setHint(getString(R.string.login_password));
            etNewPassword.setHint(getString(R.string.confirm_password));
            tvTitle.setText(getString(R.string.register));
            btnRegister.setText(getString(R.string.register_title));
        }else {
            tvTitle.setText(getString(R.string.modify_password_title));
            btnRegister.setText(getString(R.string.modify_button_title));
        }

        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);
        registerBarTheme.setBackgroundColor(Color.parseColor(preferences.getString("theme_color","#474747")));
    }

    @OnClick({R.id.register_back,R.id.btn_register})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.register_back:
                finish();
                break;
            case R.id.btn_register:
                if (TextUtils.isEmpty(etAccount.getText())||TextUtils.isEmpty(etNewPassword.getText())
                        ||TextUtils.isEmpty(etOldPassword.getText())){
                    Toast.makeText(this, "账户或密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.show();
                disposable.add(HttpUtil.getInstance().checkLogin(type+1,etAccount.getText().toString()
                        ,etOldPassword.getText().toString(),etNewPassword.getText().toString())
                        .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        dialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "请检查网络", Toast.LENGTH_SHORT).show();
                    }
                }));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.clear();
        if (MyUtil.isSoftInputMethodShowing(this)) {
            inputMethodManager.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
