package cn.edu.ustc.software.hanyizhao.encryptiontool;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import cn.edu.ustc.software.hanyizhao.encryptiontool.service.StaticData;

public class ModifyPasswordFragment extends Fragment implements FragmentHandler.OnModifyFinishListener {


    public ModifyPasswordFragment() {

    }

    FragmentHandler handler;
    ProgressDialog progressDialog;
    TextView textView1, textView2, textView3;

    public static ModifyPasswordFragment newInstance() {
        return new ModifyPasswordFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        handler = new FragmentHandler();
        handler.setOnModifyFinishListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_modify_password, container, false);
        textView1 = (TextView) root.findViewById(R.id.modify_password_old_password);
        textView2 = (TextView) root.findViewById(R.id.modify_password_new_password);
        textView3 = (TextView) root.findViewById(R.id.modify_password_new_password_repeat);
        return root;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.modify_password_menu_done: {
                hideIME();
                String p1 = textView1.getText().toString();
                final String p2 = textView2.getText().toString();
                String p3 = textView3.getText().toString();

                boolean flag = true;
                String errorMessage = null;

                if (!p1.equals(StaticData.getInstance().getRealPassword())) {
                    flag = false;
                    errorMessage = "密码错误";
                } else if (p2.length() < 3) {
                    flag = false;
                    errorMessage = "新密码长度太短！";
                } else if (!p2.equals(p3)) {
                    flag = false;
                    errorMessage = "新密码两次输入不相同！";
                }
                if (!flag) {
                    StaticData.getInstance().showMessage(errorMessage, this.getContext(), Toast.LENGTH_LONG);
                } else {
                    progressDialog = ProgressDialog.show(this.getContext(),
                            null, getString(R.string.modify_login_password_ing));
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            Message message = handler.obtainMessage();
                            message.arg1 = FragmentHandler.MESSAGE_MODIFY_FINISH;
                            if (StaticData.getInstance().modifyLoginPassword(p2)) {
                                message.arg2 = FragmentHandler.MESSAGE_MODIFY_FINISH_SUCCESS;
                            } else {
                                message.arg2 = FragmentHandler.MESSAGE_MODIFY_FINISH_FAIL;
                            }
                            message.sendToTarget();
                        }
                    };
                    thread.start();
                }
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!((MainActivity) getActivity()).mNavigationDrawerFragment.isDrawerOpen()) {
            inflater.inflate(R.menu.modify_password_menu, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //((MainActivity) context).onSectionAttached(3);
    }

    @Override
    public void onModifyFinish(boolean result) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        textView1.setText("");
        textView2.setText("");
        textView3.setText("");
        if (result) {
            StaticData.getInstance().showMessage("修改登录密码完成！", this.getContext());
        } else {
            StaticData.getInstance().showMessage("修改登录密码失败！", this.getContext());
        }
    }

    private void hideIME() {
        InputMethodManager imi = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imi.isActive()) {
            imi.hideSoftInputFromWindow(textView1.getWindowToken(), 0);
            imi.hideSoftInputFromWindow(textView2.getWindowToken(), 0);
            imi.hideSoftInputFromWindow(textView3.getWindowToken(), 0);
        }
    }
}
