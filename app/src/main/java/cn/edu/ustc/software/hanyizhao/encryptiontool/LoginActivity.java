package cn.edu.ustc.software.hanyizhao.encryptiontool;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.security.Permission;
import java.security.Permissions;
import java.util.jar.Manifest;

import cn.edu.ustc.software.hanyizhao.encryptiontool.service.StaticData;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.DBTools;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.Secret;

public class LoginActivity extends AppCompatActivity implements FragmentHandler.OnLoadingFinishListener, TextWatcher {

    int menu = 0;
    FragmentHandler handler;
    ProgressDialog progressDialog;
    TextView loginTextView;

    private String password = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new FragmentHandler();

        //检查读SD卡权限
        if (Build.VERSION.SDK_INT >= 23) {
            int check = checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (check != PackageManager.PERMISSION_GRANTED) {
                if (!shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.needStoragePermission)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.setData(Uri.fromParts("package", getPackageName(), null));
                                    startActivityForResult(intent, 2);
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .create()
                            .show();
                } else {
                    requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
            } else {
                setView();
            }
        } else {
            setView();
        }


    }

    private void setView() {
        DBTools dbTools = DBTools.getInstance();
        if (!dbTools.initConnection()) {
            //创建数据库或者读取数据库失败
            this.setTitle(getString(R.string.no_sd_card));
            StaticData.getInstance().showMessage(getString(R.string.no_sd_card), this);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        } else {
            String a = dbTools.getProperty(StaticData.DB_LOGIN_PASSWORD);
            dbTools.close();
            if (a == null) {
                menu = R.menu.menu_register;
                this.setTitle(R.string.set_password);
                setContentView(R.layout.activity_register);
            } else {
                password = a;
                handler.setOnLoadingFinishListener(this);
                menu = R.menu.menu_login;
                this.setTitle(R.string.login);
                setContentView(R.layout.activity_login);
                loginTextView = ((TextView) findViewById(R.id.login_editText_password));
                loginTextView.addTextChangedListener(this);
            }
            supportInvalidateOptionsMenu();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            setView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        setView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (this.menu != 0) {
            getMenuInflater().inflate(this.menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_register_OK) {
            String s1 = ((EditText) findViewById(R.id.register_editText_password)).getText().toString();
            String s2 = ((EditText) findViewById(R.id.register_editText_repeat)).getText().toString();
            if (s1.length() < 3) {
                StaticData.getInstance().showMessage("密码太短！", this);
                return true;
            }
            if (!s1.equals(s2)) {
                StaticData.getInstance().showMessage("两次输入不一致！", this);
                return true;
            }
            DBTools dbTools = DBTools.getInstance();
            dbTools.setProperty(StaticData.DB_LOGIN_PASSWORD, Secret.SHAPassword(Secret.SHAPassword(s1 + StaticData.finalMixPasswordString)));
            dbTools.close();
            Intent i = new Intent();
            i.setClass(this, LoginActivity.class);
            startActivity(i);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean tryToLogin() {
        String s = ((EditText) findViewById(R.id.login_editText_password)).getText().toString();
        s = Secret.SHAPassword(Secret.SHAPassword(s + StaticData.finalMixPasswordString));
        if (s.equals(password)) {
            progressDialog = ProgressDialog.show(this, null, getString(R.string.loading___));
            Thread thread = new Thread() {
                @Override
                public void run() {
                    StaticData.getInstance().setRealPassword(((EditText) findViewById(R.id.login_editText_password)).getText().toString());
                    StaticData.getInstance().resetData();
                    Message message = handler.obtainMessage();
                    message.arg1 = FragmentHandler.MESSAGE_LOADING_FINISH;
                    message.sendToTarget();
                }
            };
            thread.start();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onLoadingFinish() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(findViewById(R.id.login_editText_password).getApplicationWindowToken(), 0);
        }
        Intent i = new Intent();
        i.setClass(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        //登录时，输入密码时触发
        tryToLogin();
    }
}
