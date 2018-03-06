package cn.edu.ustc.software.hanyizhao.encryptiontool;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import cn.edu.ustc.software.hanyizhao.encryptiontool.service.StaticData;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.AESTools;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.FileChooser;
import cn.edu.ustc.software.hanyizhao.encryptiontool.bean.Password;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.Secret;

public class PasswordExportActivity extends HandleSavedDataActivity implements View.OnClickListener {

    EditText passwordE = null;
    EditText passwordE2 = null;
    EditText fileN = null;
    EditText fileE = null;
    Button chooseFile = null;
    File cFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_export);
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        passwordE = (EditText) this.findViewById(R.id.export_data_password);
        passwordE2 = (EditText) this
                .findViewById(R.id.export_data_password_ensure);
        fileE = (EditText) this.findViewById(R.id.export_data_file);
        fileN = (EditText) this.findViewById(R.id.export_data_name);
        chooseFile = (Button) this.findViewById(R.id.export_data_file_button);
        chooseFile.setOnClickListener(this);

        passwordE.setText(StaticData.getInstance().getRealPassword());
        passwordE2.setText(StaticData.getInstance().getRealPassword());

        fileN.setText(new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.CHINA)
                .format(new Date()));
        cFile = Environment.getExternalStorageDirectory();
        fileE.setText(cFile.getPath());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.password_menu_export, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.password_menu_export_done:
                hideIME();
                OK();
                break;
            default: {
                hideIME();
                finish();
                break;
            }

        }

        return super.onOptionsItemSelected(item);
    }

    private void OK() {
        if (cFile == null) {
            new AlertDialog.Builder(this).setTitle("错误")
                    .setMessage("请选择数据文件路径！")
                    .setPositiveButton(R.string.OK, null).show();
            return;
        }
        final String nPassword = passwordE.getText().toString();
        String nPassword2 = passwordE2.getText().toString();
        if (!nPassword.equals(nPassword2)) {
            new AlertDialog.Builder(this).setTitle("错误")
                    .setMessage("两次输入密码不同！")
                    .setPositiveButton(R.string.OK, null).show();
            return;
        }
        final File targetFile = new File(cFile.getAbsolutePath() + "/"
                + fileN.getText().toString() + ".secbak2");
        if (targetFile.exists()) {
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("该文件已存在，确认覆盖吗？")
                    .setPositiveButton(R.string.OK,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    save(nPassword, targetFile);
                                }
                            }).setNegativeButton(R.string.Cancel, null).show();
        } else {
            save(nPassword, targetFile);
        }
    }

    private void save(String password, File file) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file, false));
            bw.write(Secret.SHAPassword(password + StaticData.finalMixPasswordString));
            bw.write("\n");
            JSONObject jo = new JSONObject();
            JSONArray ja = new JSONArray();
            jo.put("data", ja);
            Iterator<Password> i = StaticData.getInstance().data.iterator();
            int count = 0;
            while (i.hasNext()) {
                Password temp = i.next();
                JSONObject tempj = new JSONObject();
                tempj.put("time", temp.time);
                tempj.put("lasttime", temp.lastTime);
                tempj.put("name", temp.name);
                tempj.put("message1", temp.userName);
                tempj.put("message2", temp.password);
                tempj.put("tip", temp.tip);
                ja.put(tempj);
                count++;
            }
            AESTools tool = new AESTools(Secret.SHAPassword(password
                    + StaticData.finalMixPassword2).getBytes("UTF-8"));
            String a = Secret.bytesToHexString(tool.Encrypt(jo.toString()
                    .getBytes("UTF-8")));
            if (a != null) {
                bw.write(a);
            }
            StaticData.getInstance().showMessage("成功导出" + count + "条", this);
            finish();
        } catch (Exception e) {
            if (e.getMessage().contains("ENOENT")) {
                StaticData.getInstance().showMessage("文件路径不合法", this);
            } else if (e.getMessage().contains("EROFS")) {
                StaticData.getInstance().showMessage("文件没有写权限，请更换目录", this);
            } else {
                StaticData.getInstance().showMessage(e.getMessage(), this);
                e.printStackTrace();
            }
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == chooseFile) {
            new FileChooser().getFilePath(this, new String[]{},
                    this.getString(R.string.choose_data_path),
                    new FileChooser.selectOneFileResult() {
                        @Override
                        public void selectOneFile(File f) {
                            cFile = f;
                            fileE.setText(f.getPath());
                        }
                    }, FileChooser.FileMode.ONEDICTIONARY);
        }
    }

    private void hideIME() {
        InputMethodManager imi = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imi.isActive()) {
            imi.hideSoftInputFromWindow(passwordE.getWindowToken(), 0);
            imi.hideSoftInputFromWindow(passwordE2.getWindowToken(), 0);
            imi.hideSoftInputFromWindow(fileN.getWindowToken(), 0);

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

}
