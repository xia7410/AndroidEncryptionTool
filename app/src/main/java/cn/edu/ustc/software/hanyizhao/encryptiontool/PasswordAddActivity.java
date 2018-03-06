package cn.edu.ustc.software.hanyizhao.encryptiontool;

import android.support.v7.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import cn.edu.ustc.software.hanyizhao.encryptiontool.service.StaticData;
import cn.edu.ustc.software.hanyizhao.encryptiontool.bean.Password;

public class PasswordAddActivity extends HandleSavedDataActivity implements View.OnClickListener {

    boolean isShowPassword = false;
    EditText editText;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_add);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        editText = (EditText) findViewById(R.id.password_editText_password);
        button = ((Button) findViewById(R.id.password_button_see));
        button.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.password_menu_add, menu);
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
            case R.id.password_menu_add_save:
                Password p = new Password();
                p.name = ((EditText) findViewById(R.id.password_editText_name)).getText().toString();
                p.userName = ((EditText) findViewById(R.id.password_editText_username)).getText().toString();
                p.password = editText.getText().toString();
                p.tip = ((EditText) findViewById(R.id.password_editText_tip)).getText().toString();
                if (p.name.trim().length() == 0) {
                    StaticData.getInstance().showMessage("项目名称不能为空！", this);
                    return true;
                }
                p.lastTime = StaticData.createTime();
                p.time = p.lastTime + "0001";
                if (StaticData.getInstance().hasPassword(p)) {
                    StaticData.getInstance().showMessage("已存在！", this);
                    return true;
                }
                if (StaticData.getInstance().addPassword(this, p)) {
                    setResult(RESULT_OK);
                    hideIME();
                    finish();
                } else {
                    StaticData.getInstance().showMessage("添加失败！", this);
                }
                break;
            default:
                setResult(RESULT_CANCELED);
                hideIME();
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void hideIME() {
        InputMethodManager imi = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imi.isActive()) {
            imi.hideSoftInputFromWindow(findViewById(R.id.password_editText_name).getWindowToken(), 0);
            imi.hideSoftInputFromWindow(findViewById(R.id.password_editText_username).getWindowToken(), 0);
            imi.hideSoftInputFromWindow(findViewById(R.id.password_editText_password).getWindowToken(), 0);
            imi.hideSoftInputFromWindow(findViewById(R.id.password_editText_tip).getWindowToken(), 0);
        }
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
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD
                    | InputType.TYPE_CLASS_TEXT);
            button.setText(R.string.see);
        } else {
            editText
                    .setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            button.setText(R.string.hide);
        }
        isShowPassword = !isShowPassword;
    }
}
