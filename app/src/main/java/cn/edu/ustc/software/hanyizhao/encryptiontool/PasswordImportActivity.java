package cn.edu.ustc.software.hanyizhao.encryptiontool;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.edu.ustc.software.hanyizhao.encryptiontool.service.StaticData;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.AESTools;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.FileChooser;
import cn.edu.ustc.software.hanyizhao.encryptiontool.bean.Password;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.Secret;

public class PasswordImportActivity extends HandleSavedDataActivity implements View.OnClickListener {

    EditText passwordE = null;
    EditText fileE = null;
    Button chooseFile = null;
    CheckBox checkBox = null;
    File cFile = null;


    private TextView clearDialogt1, clearDialogt2;
    private ProgressBar clearDialogPro;
    private AlertDialog alertDialog;
    private MyHandler myHandler = new MyHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_import);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        passwordE = (EditText) this.findViewById(R.id.import_data_password);
        fileE = (EditText) this.findViewById(R.id.import_data_file);
        chooseFile = (Button) this.findViewById(R.id.import_data_file_button);
        checkBox = (CheckBox) this.findViewById(R.id.import_data_checkBox);
        chooseFile.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.password_menu_import, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.password_menu_import_done:
                OK();
                break;
            default:
                hideIME();
                setResult(RESULT_CANCELED);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void OK() {
        if (cFile == null) {
            new AlertDialog.Builder(this).setTitle("错误").setMessage("请选择数据文件！")
                    .setPositiveButton(R.string.OK, null).show();
            return;
        }
        int version = 1;//老版本 2新版本
        if (cFile.getName().endsWith("2")) {
            version = 2;
        }
        String p = passwordE.getText().toString();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(cFile));
            String temp = br.readLine();
            if ((version == 2 ? Secret.SHAPassword(p + StaticData.finalMixPasswordString)
                    : Secret.SHAPassword(p)).equals(temp)) {
                AESTools tools;
                if (version == 2) {
                    tools = new AESTools(Secret.SHAPassword(p + StaticData.finalMixPassword2).getBytes("UTF-8"));
                } else {
                    tools = new AESTools(p.getBytes("UTF-8"));
                }
                temp = br.readLine();
                br.close();
                JSONObject jo;
                try {//1高版本进行了加密
                    jo = new JSONObject(new String(tools.Decrypt(Secret
                            .hexStringToBytes(temp)), "UTF-8"));
                } catch (Exception e) {
                    //1低版本未加密
                    jo = new JSONObject(new String(
                            Secret.hexStringToBytes(temp), "UTF-8"));
                }
                int allSameCount0 = 0;
                final List<Password> exitNeedUpdate1 = new ArrayList<>();
                int existButOld2 = 0;
                final List<Password> allDifferentList3 = new ArrayList<>();
                int notExistButSame4 = 0;

                JSONArray ja = jo.optJSONArray("data");
                String date = StaticData.createTime();
                for (int i = 0; i < ja.length(); i++) {
                    Password tempp = new Password();
                    JSONObject tempo = ja.getJSONObject(i);
                    tempp.name = tempo.getString("name");
                    tempp.userName = tempo.getString("message1");
                    tempp.password = tempo.getString("message2");
                    tempp.tip = tempo.getString("tip");
                    tempp.time = version == 2 ? tempo.getString("time") : date + String.format("%04d", i);
                    tempp.lastTime = version == 2 ? tempo.getString("lasttime") : date;
                    if (version == 1) {
                        if (!StaticData.getInstance().hasPassword(tempp)) {
                            allDifferentList3.add(tempp);
                        } else {
                            notExistButSame4++;
                        }
                    } else {
                        int r = StaticData.getInstance().hasPasswordByTime(tempp);
                        switch (r) {
                            case 0:
                                allSameCount0++;
                                break;
                            case 1:
                                exitNeedUpdate1.add(tempp);
                                break;
                            case 2:
                                existButOld2++;
                                break;
                            case 3:
                                allDifferentList3.add(tempp);
                                break;
                            case 4:
                                notExistButSame4++;
                                break;
                        }
                    }
                }
                final Builder builder = new Builder(this).setTitle("添加项目");
                StringBuilder sb = new StringBuilder("确认：\n");
                if (allDifferentList3.size() != 0) {
                    sb.append("添加");
                    sb.append(allDifferentList3.size());
                    sb.append("条全新的密码项\n");
                }
                if (exitNeedUpdate1.size() != 0) {
                    sb.append("更新");
                    sb.append(exitNeedUpdate1.size());
                    sb.append("条密码项\n");
                }
                if (allSameCount0 + notExistButSame4 != 0) {
                    sb.append("有");
                    sb.append(allSameCount0 + notExistButSame4);
                    sb.append("条重复密码项\n");
                }
                if (existButOld2 != 0) {
                    sb.append("有");
                    sb.append(existButOld2);
                    sb.append("条旧的密码项");
                }
                String message = sb.toString();
                builder.setNegativeButton(R.string.Cancel, null);
                if (message.length() > 6) {
                    builder.setMessage(message);
                    if (exitNeedUpdate1.size() != 0 || allDifferentList3.size() != 0) {
                        builder.setPositiveButton(R.string.OK,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        LinearLayout v = (LinearLayout) LayoutInflater.from(PasswordImportActivity.this)
                                                .inflate(R.layout.progressbar_dialog, (ViewGroup) findViewById(R.id.progressbar_dialog));
                                        clearDialogPro = (ProgressBar) v
                                                .findViewById(R.id.dialog_clear_progressBar);
                                        clearDialogt1 = (TextView) v
                                                .findViewById(R.id.dialog_clear_text1);
                                        clearDialogt2 = (TextView) v
                                                .findViewById(R.id.dialog_clear_text2);
                                        clearDialogPro.setMax(allDifferentList3.size() + exitNeedUpdate1.size());
                                        clearDialogPro.setProgress(0);
                                        alertDialog = new Builder(PasswordImportActivity.this).setCancelable(false)
                                                .setTitle(R.string.import_).setView(v).show();
                                        Thread t = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                int max = allDifferentList3.size() + exitNeedUpdate1.size();
                                                int count = 0;
                                                int fail = 0;
                                                for (Password i : allDifferentList3) {
                                                    if (!StaticData.getInstance().addPassword(PasswordImportActivity.this,
                                                            i)) {
                                                        fail++;
                                                    }
                                                    count++;
                                                    Message m = myHandler.obtainMessage();
                                                    m.arg1 = 1;
                                                    Bundle b = new Bundle();
                                                    b.putInt("max", max);
                                                    b.putInt("now", count);
                                                    m.setData(b);
                                                    m.sendToTarget();
                                                }
                                                for (Password i : exitNeedUpdate1) {
                                                    if (!StaticData.getInstance().setPasswordByTime(PasswordImportActivity.this,
                                                            i)) {
                                                        fail++;
                                                    }
                                                    count++;
                                                    Message m = myHandler.obtainMessage();
                                                    m.arg1 = 1;
                                                    Bundle b = new Bundle();
                                                    b.putInt("max", max);
                                                    b.putInt("now", count);
                                                    m.setData(b);
                                                    m.sendToTarget();
                                                }
                                                String ass = "";
                                                if (checkBox.isChecked()) {
                                                    if (cFile.isFile()
                                                            && cFile.canWrite()) {
                                                        boolean f = cFile.delete();
                                                        if (f) {
                                                            ass = "源文件已删除";
                                                        } else {
                                                            ass = "源文件删除失败";
                                                        }
                                                    }
                                                }

                                                Message m = myHandler.obtainMessage();
                                                m.arg1 = 0;
                                                Bundle b = new Bundle();
                                                b.putString("data", "成功导入" + (count - fail) + "条  " + (fail == 0 ? "" : "失败" + fail + "条 ") + ass);
                                                m.setData(b);
                                                m.sendToTarget();
                                            }
                                        });
                                        t.start();
                                    }
                                });
                    }
                } else {
                    builder.setMessage("文件中无密码");
                }
                builder.show();


            } else {
                new AlertDialog.Builder(this).setTitle("错误")
                        .setMessage("密码错误！")
                        .setPositiveButton(R.string.OK, null).show();
            }
        } catch (Exception e) {
            new AlertDialog.Builder(this).setTitle("错误")
                    .setMessage(e.getMessage())
                    .setPositiveButton(R.string.OK, null).show();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.arg1 == 0) {
                alertDialog.cancel();
                StaticData.getInstance().showMessage(msg.getData().getString("data"), PasswordImportActivity.this);
                setResult(RESULT_OK);
                hideIME();
                PasswordImportActivity.this.finish();
            } else {
                Bundle b = msg.getData();
                int max = b.getInt("max");
                int now = b.getInt("now");
                if (now < max) {
                    now++;
                }
                clearDialogt1.setText(now + "/" + max);
                clearDialogt2.setText((int) (((float) now) / max * 100) + "%");
                clearDialogPro.setProgress(now);
                clearDialogPro.setMax(max);
            }

        }
    }

    private void hideIME() {
        InputMethodManager imi = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imi.isActive()) {
            imi.hideSoftInputFromWindow(passwordE.getWindowToken(), 0);
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
        if (v == chooseFile) {
            new FileChooser().getFilePath(this, new String[]{".secbak", ".secbak2"},
                    this.getString(R.string.choose_data_file),
                    new FileChooser.selectOneFileResult() {
                        @Override
                        public void selectOneFile(File f) {
                            cFile = f;
                            fileE.setText(f.getName());
                        }
                    }, FileChooser.FileMode.ONEFILE);
        }

    }
}
