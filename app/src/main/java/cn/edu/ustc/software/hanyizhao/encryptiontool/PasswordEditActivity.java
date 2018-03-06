package cn.edu.ustc.software.hanyizhao.encryptiontool;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import cn.edu.ustc.software.hanyizhao.encryptiontool.service.StaticData;
import cn.edu.ustc.software.hanyizhao.encryptiontool.bean.Password;

public class PasswordEditActivity extends HandleSavedDataActivity implements View.OnClickListener {

    Button button;
    TextView textViewTime, textViewLastTime;
    EditText editTextName, editTextUserName, editTextPassword, editTextTip;
    boolean isShowPassword = false;
    Password oldPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_edit);
        button = (Button) findViewById(R.id.password_button_see);
        textViewTime = (TextView) findViewById(R.id.password_textView_edit_time);
        textViewLastTime = ((TextView) findViewById(R.id.password_textView_last_time));
        editTextName = (EditText) findViewById(R.id.password_editText_edit_name);
        editTextUserName = (EditText) findViewById(R.id.password_editText_edit_username);
        editTextPassword = (EditText) findViewById(R.id.password_editText_edit_password);
        editTextTip = (EditText) findViewById(R.id.password_editText_edit_tip);
        oldPassword = (Password) getIntent().getSerializableExtra("data");
        textViewTime.setText(oldPassword.getStringTime());
        textViewLastTime.setText(oldPassword.getStringLastTime());
        editTextName.setText(oldPassword.name);
        editTextUserName.setText(oldPassword.userName);
        editTextPassword.setText(oldPassword.password);
        editTextTip.setText(oldPassword.tip);
        button.setOnClickListener(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.password_menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.password_menu_edit_save: {
                Password p = new Password();
                p.id = oldPassword.id;
                p.time = oldPassword.time;
                p.name = editTextName.getText().toString();
                p.userName = editTextUserName.getText().toString();
                p.password = editTextPassword.getText().toString();
                p.tip = editTextTip.getText().toString();
                if (p.equals(oldPassword)) {
                    StaticData.getInstance().showMessage("内容无变化，未修改！", this);
                    setResult(RESULT_CANCELED);
                    hideIME();
                    finish();
                } else {
                    if (p.name.trim().length() == 0) {
                        StaticData.getInstance().showMessage("项目名不能为空！", this);
                    } else {
                        if (StaticData.getInstance().setPasswordById(this, p)) {
                            StaticData.getInstance().showMessage("保存成功！", this);
                            hideIME();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            StaticData.getInstance().showMessage("保存失败！", this);
                        }
                    }
                }
                break;
            }
            case R.id.password_menu_edit_delete: {
                final Password p = oldPassword;
                Builder b = new Builder(this);
                b.setTitle(R.string.delete);
                b.setMessage("确认删除密码项：" + p.name + "吗？");
                b.setNegativeButton(R.string.Cancel, null);
                b.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (StaticData.getInstance().deletePassword(PasswordEditActivity.this, p)) {
                            StaticData.getInstance().showMessage("删除成功！", PasswordEditActivity.this);
                            hideIME();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            StaticData.getInstance().showMessage("删除失败！", PasswordEditActivity.this);
                        }
                    }
                });
                b.show();
                break;
            }
            default:
                setResult(RESULT_CANCELED);
                hideIME();
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (event.getRepeatCount() == 0) {
                    hideIME();
                    finish();
                    return true;
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        if (isShowPassword) {
            editTextPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD
                    | InputType.TYPE_CLASS_TEXT);
            button.setText(R.string.see);
        } else {
            editTextPassword
                    .setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            button.setText(R.string.hide);
        }
        isShowPassword = !isShowPassword;
    }

    private void hideIME() {
        InputMethodManager imi = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imi.isActive()) {
            imi.hideSoftInputFromWindow(editTextName.getWindowToken(), 0);
            imi.hideSoftInputFromWindow(editTextUserName.getWindowToken(), 0);
            imi.hideSoftInputFromWindow(editTextPassword.getWindowToken(), 0);
            imi.hideSoftInputFromWindow(editTextTip.getWindowToken(), 0);
        }
    }

}
