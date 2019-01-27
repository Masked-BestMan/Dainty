package com.zbm.dainty.login;

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
import com.zbm.dainty.util.MyUtil;
import com.zbm.dainty.widget.LoadingDialog;
import com.zbm.dainty.widget.SwipeBackActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegisterActivity extends SwipeBackActivity implements LoginRegisterContract.View{

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
    private LoadingDialog dialog;
    private int type;
    private LoginRegisterPresenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);
        registerBarTheme.setBackgroundColor(Color.parseColor(preferences.getString("theme_color","#474747")));
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

        presenter=new LoginRegisterPresenter(this);
        dialog=new LoadingDialog(this,"注册中");
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
                if (type==0) {
                    presenter.register(etAccount.getText().toString(), etOldPassword.getText()
                            .toString(), etNewPassword.getText().toString());
                }else {
                    presenter.modifyPassword(etAccount.getText().toString(), etOldPassword
                            .getText().toString(), etNewPassword.getText().toString());
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.unsubscribe();
        if (MyUtil.isSoftInputMethodShowing(this)) {
            inputMethodManager.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void showToast(String msg) {
        dialog.dismiss();
        Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
        if (msg.contains("成功")){
            finish();
        }
    }
}
