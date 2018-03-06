package cn.edu.ustc.software.hanyizhao.encryptiontool;

/**
 * Created by HanYizhao on 2015/9/25.
 * 密码页面
 */

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import cn.edu.ustc.software.hanyizhao.encryptiontool.bean.Password;
import cn.edu.ustc.software.hanyizhao.encryptiontool.service.StaticData;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.Logger;

/**
 * A placeholder fragment containing a simple view.
 */
public class PasswordFragment extends Fragment implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener, CompoundButton.OnCheckedChangeListener, View.OnTouchListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */


    public static final int requestCode_ADDPASSWORD = 1;
    public static final int requestCode_EDITPASSWORD = 2;
    public static final int requestCode_IMPORTPASSWORD = 3;


    private EditText editTextPasswordSearch = null;
    private TextView searchSum = null;
    public MyPasswordListViewAdapter myPasswordListViewAdapter = null;
    private ListView listViewPassword = null;

    public List<Password> passwordSource = new ArrayList<>();
    public HashSet<Integer> passwordSelected = new HashSet<>();
    public boolean isInMultiSelect = false;


    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */

    public static PasswordFragment newInstance() {
        return new PasswordFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Logger.e("PasswordFragment", "onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    View rootView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Logger.e("PasswordFragment", "onCreateView");
        //创建视图
        //密码管理
        rootView = inflater.inflate(R.layout.fragment_password, container, false);
        imm = (InputMethodManager) this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        listViewPassword = (ListView) rootView.findViewById(R.id.password_list_view);
        editTextPasswordSearch = (EditText) rootView.findViewById(R.id.password_editText_search);
        searchSum = (TextView) rootView.findViewById(R.id.password_textView_sum);
        myPasswordListViewAdapter = new MyPasswordListViewAdapter(this.getContext());
        listViewPassword.setAdapter(myPasswordListViewAdapter);
        listViewPassword.setOnTouchListener(this);
        editTextPasswordSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                refreshPasswordListView();
                if (isInMultiSelect) {
                    getActivity().supportInvalidateOptionsMenu();
                }
            }
        });
        listViewPassword.setOnItemClickListener(this);
        listViewPassword.setOnItemLongClickListener(this);
        refreshPasswordListView();
        return rootView;
    }

    private InputMethodManager imm;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.password_list_view) {
            if (imm.isActive()) {
                imm.hideSoftInputFromWindow(editTextPasswordSearch.getApplicationWindowToken(), 0);
            }
        }
        return false;
    }


    public class MyPasswordListViewAdapter extends BaseAdapter {

        LayoutInflater la;
        Context c;

        public MyPasswordListViewAdapter(Context a) {
            this.c = a;
            la = LayoutInflater.from(c);
        }

        @Override
        public int getCount() {
            return passwordSource.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = la.inflate(R.layout.password_list_view, null);
                viewHolder.name = (TextView) convertView.findViewById(R.id.password_list_view_name);
                viewHolder.username = (TextView) convertView.findViewById(R.id.password_list_view_username);
                viewHolder.tip = (TextView) convertView.findViewById(R.id.password_list_view_tip);
                viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.password_list_view_checkBox);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Password p = passwordSource.get(position);
            if (p != null) {
                if (isInMultiSelect) {
                    viewHolder.checkBox.setOnCheckedChangeListener(null);
                    viewHolder.checkBox.setVisibility(View.VISIBLE);
                    if (passwordSelected.contains(position)) {
                        viewHolder.checkBox.setChecked(true);
                    } else {
                        viewHolder.checkBox.setChecked(false);
                    }
                    viewHolder.checkBox.setOnCheckedChangeListener(PasswordFragment.this);
                } else {
                    viewHolder.checkBox.setVisibility(View.GONE);
                }
                viewHolder.checkBox.setTag(position);
                viewHolder.name.setText(p.name);
                viewHolder.username.setText(p.userName);
                viewHolder.tip.setText(p.tip);
            }
            return convertView;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            if (searchSum != null) {
                String a;
                if (isInMultiSelect) {
                    a = passwordSelected.size() + "/" + passwordSource.size();
                } else {
                    a = passwordSource.size() + "条";
                }
                searchSum.setText(a);
            }
        }

        class ViewHolder {
            public CheckBox checkBox;
            public TextView name;
            public TextView username;
            public TextView tip;
            public boolean isNormal = true;
        }

    }


    /**
     * 刷新数据源，进行选择，排序
     */
    private void filterPasswordSource() {
        if (!isInMultiSelect) {
            passwordSource.clear();
            passwordSelected.clear();
            doSearchPassword();
            doSort();
        } else {
            List<Password> tempPasswordSource = new ArrayList<>();
            HashSet<Integer> tempPasswordSelected = new HashSet<>();
            doSearchPassword(tempPasswordSource);
            doSort(tempPasswordSource);
            int id;
            for (int i = 0; i < passwordSource.size(); i++) {
                if (passwordSelected.contains(i)) {
                    id = tempPasswordSource.indexOf(passwordSource.get(i));
                    if (id != -1) {
                        tempPasswordSelected.add(id);
                    }
                }
            }
            passwordSelected = null;
            passwordSelected = tempPasswordSelected;
            passwordSource = null;
            passwordSource = tempPasswordSource;
        }
    }

    private void doSort() {
        doSort(passwordSource);
    }

    private void doSearchPassword() {
        doSearchPassword(passwordSource);
    }

    /**
     * 对搜索后的结果排序
     */
    private void doSort(List<Password> passwordSource) {
        Collections.sort(passwordSource, new Comparator<Password>() {
            @Override
            public int compare(Password lhs, Password rhs) {
                int result;
                switch (StaticData.getInstance().passwordSortOptions) {
                    case 1:
                        result = lhs.time.compareTo(rhs.time);
                        break;
                    case 2:
                        result = lhs.name.compareTo(rhs.name);
                        break;
                    case 3:
                        result = lhs.userName.compareTo(rhs.userName);
                        break;
                    default:
                        result = lhs.password.compareTo(rhs.password);
                        break;
                }
                if (!StaticData.getInstance().passwordSortOptionsUp) {
                    result = -result;
                }
                return result;
            }
        });
    }

    /**
     * 检查密码是否符合搜索条件
     */
    private void doSearchPassword(List<Password> passwordSource) {
        if (editTextPasswordSearch == null || editTextPasswordSearch.getText().toString().length() == 0) {
            for (Password i : StaticData.getInstance().data) {
                passwordSource.add(i);
            }
            return;
        }
        String a = editTextPasswordSearch.getText().toString().toLowerCase();
        String name, password, userName, tip;
        boolean flag;
        for (Password temp : StaticData.getInstance().data) {
            flag = false;
            name = temp.name.toLowerCase();
            password = temp.password.toLowerCase();
            userName = temp.userName.toLowerCase();
            tip = temp.tip.toLowerCase();
            switch (StaticData.getInstance().passwordSearchOptions) {
                case 6:
                    //全部
                    if (name.contains(a) || password.contains(a)
                            || userName.contains(a) || tip.contains(a)) {
                        flag = true;
                    }
                    break;
                case 1:
                    if (name.contains(a) || userName.contains(a) || tip.contains(a)) {
                        flag = true;
                    }
                    break;
                case 2:
                    if (name.contains(a)) {
                        flag = true;
                    }
                    break;
                case 3:
                    if (userName.contains(a)) {
                        flag = true;
                    }
                    break;
                case 4:
                    if (tip.contains(a)) {
                        flag = true;
                    }
                    break;
                case 5:
                    if (password.contains(a)) {
                        flag = true;
                    }
                    break;
            }
            if (flag) {
                passwordSource.add(temp);
            }
        }

    }


    /**
     * 处理密码搜索菜单选中之后的行为
     *
     * @param value 搜索选项
     */
    private void handlePasswordSearchMenu(int value) {
        StaticData.getInstance().setPasswordSearchOptions(this.getContext(), value);
        refreshPasswordListView();
        getActivity().supportInvalidateOptionsMenu();
    }

    /**
     * 处理密码排序菜单选中之后的行为
     *
     * @param value 排序选项
     */
    private void handlePasswordSortMenu(int value) {
        if (StaticData.getInstance().passwordSortOptions == value) {
            StaticData.getInstance().setPasswordSortOptions(this.getContext(),
                    value, !StaticData.getInstance().passwordSortOptionsUp);
        } else {
            StaticData.getInstance().setPasswordSortOptions(this.getContext(), value, true);
        }
        refreshPasswordListView();
        getActivity().supportInvalidateOptionsMenu();
    }


    public void refreshPasswordListView() {
        filterPasswordSource();

        myPasswordListViewAdapter.notifyDataSetChanged();
        listViewPassword.setSelection(0);
    }

    @Override
    public void onAttach(Context context) {
        Logger.e("PasswordFragment", "onAttach");
        super.onAttach(context);
        //((MainActivity) context).onSectionAttached(0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case requestCode_EDITPASSWORD:
                if (resultCode == Activity.RESULT_OK) {
                    refreshPasswordListView();
                }
                break;
            case requestCode_ADDPASSWORD:
                if (resultCode == Activity.RESULT_OK) {
                    refreshPasswordListView();
                }
                break;
            case requestCode_IMPORTPASSWORD:
                if (resultCode == Activity.RESULT_OK) {
                    refreshPasswordListView();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent == listViewPassword) {
            if (isInMultiSelect) {
                if (passwordSelected.contains(position)) {
                    removeSelection(position);
                } else {
                    addSelection(position);
                }
            } else {
                Password password = passwordSource.get(position);
                if (password != null) {
                    Intent intent = new Intent();
                    intent.setClass(this.getContext(), PasswordEditActivity.class);
                    intent.putExtra("data", password);
                    startActivityForResult(intent, requestCode_EDITPASSWORD);
                }
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent == listViewPassword) {
            if (!isInMultiSelect) {
                passwordSelected.add(position);
                switchInMultiSelectPassword();
                myPasswordListViewAdapter.notifyDataSetChanged();
                return true;
            } else {
                if (!passwordSelected.contains(position)) {
                    addSelection(position);
                }
            }
        }
        return true;
    }


    /**
     * 添加选中的密码
     *
     * @param position 位置
     */
    private void addSelection(int position) {
        passwordSelected.add(position);
        myPasswordListViewAdapter.notifyDataSetChanged();
        getActivity().supportInvalidateOptionsMenu();
    }

    /**
     * 移除选中的位置
     *
     * @param position 位置
     */
    private void removeSelection(int position) {
        passwordSelected.remove(position);
        myPasswordListViewAdapter.notifyDataSetChanged();
        getActivity().supportInvalidateOptionsMenu();
    }


    /**
     * 进入选择模式
     */
    private void switchInMultiSelectPassword() {
        isInMultiSelect = true;
        getActivity().supportInvalidateOptionsMenu();
    }

    /**
     * 出选择模式
     */
    private void switchOutMultiSelectPassword() {
        isInMultiSelect = false;
        passwordSelected.clear();
        getActivity().supportInvalidateOptionsMenu();
    }


    /**
     * 当主窗口返回键按下的时候调用
     *
     * @return 如果解决，返回true
     */
    public boolean onKeyBackDown() {
        if (isInMultiSelect) {
            switchOutMultiSelectPassword();
            myPasswordListViewAdapter.notifyDataSetChanged();
            return true;
        } else {

            return false;
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        switch (id) {
            case R.id.password_list_view_checkBox:
                if (isInMultiSelect && buttonView.getTag() != null) {
                    if (isChecked) {
                        addSelection((Integer) buttonView.getTag());
                    } else {
                        removeSelection((Integer) buttonView.getTag());
                    }
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Logger.e("es", "passwordFragment_menuSelected " + item.toString());
        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.password_menu_add: {
                Intent intent = new Intent();
                intent.setClass(this.getContext(), PasswordAddActivity.class);
                startActivityForResult(intent, requestCode_ADDPASSWORD);
                return true;
            }
            case R.id.password_menu_select_all: {
                if (passwordSelected.size() == passwordSource.size()) {
                    passwordSelected.clear();
                } else {
                    for (int i = 0; i < passwordSource.size(); i++) {
                        passwordSelected.add(i);
                    }
                }
                myPasswordListViewAdapter.notifyDataSetChanged();
                getActivity().supportInvalidateOptionsMenu();
                break;
            }
            case R.id.password_menu_back: {
                switchOutMultiSelectPassword();
                myPasswordListViewAdapter.notifyDataSetChanged();
                break;
            }
            case R.id.password_menu_delete: {
                final List<Password> p = new ArrayList<>();
                for (Integer i : passwordSelected) {
                    p.add(passwordSource.get(i));
                }
                AlertDialog.Builder b = new AlertDialog.Builder(this.getContext());
                b.setTitle(R.string.delete);
                b.setMessage("确认删除这" + p.size() + "个密码项吗？");
                b.setNegativeButton(R.string.Cancel, null);
                b.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (StaticData.getInstance().deletePassword(PasswordFragment.this.getContext(), p)) {
                            StaticData.getInstance().showMessage("删除成功！", PasswordFragment.this.getContext());
                        } else {
                            StaticData.getInstance().showMessage("删除失败！", PasswordFragment.this.getContext());
                        }
                        switchOutMultiSelectPassword();
                        refreshPasswordListView();
                    }
                });
                b.show();
                break;
            }
            case R.id.password_menu_search_1:
                handlePasswordSearchMenu(1);
                break;
            case R.id.password_menu_search_2:
                handlePasswordSearchMenu(2);
                break;
            case R.id.password_menu_search_3:
                handlePasswordSearchMenu(3);
                break;
            case R.id.password_menu_search_4:
                handlePasswordSearchMenu(4);
                break;
            case R.id.password_menu_search_5:
                handlePasswordSearchMenu(5);
                break;
            case R.id.password_menu_search_6:
                handlePasswordSearchMenu(6);
                break;
            case R.id.password_menu_sort_1:
                handlePasswordSortMenu(1);
                break;
            case R.id.password_menu_sort_2:
                handlePasswordSortMenu(2);
                break;
            case R.id.password_menu_sort_3:
                handlePasswordSortMenu(3);
                break;
            case R.id.password_menu_sort_4:
                handlePasswordSortMenu(4);
                break;
            case R.id.password_menu_import: {
                Intent intent = new Intent();
                intent.setClass(this.getContext(), PasswordImportActivity.class);
                startActivityForResult(intent, requestCode_IMPORTPASSWORD);
                break;
            }
            case R.id.password_menu_export: {
                Intent intent = new Intent();
                intent.setClass(this.getContext(), PasswordExportActivity.class);
                startActivity(intent);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Logger.e("es", "passwordFragment_onCreateOptionMenu sortItem:"
                + StaticData.getInstance().passwordSortOptions + " sortUp"
                + StaticData.getInstance().passwordSortOptionsUp);
        if (!((MainActivity) getActivity()).mNavigationDrawerFragment.isDrawerOpen()) {
            inflater.inflate(R.menu.password_menu, menu);
            if (isInMultiSelect) {
                menu.findItem(R.id.password_menu_delete).setVisible(true);
                menu.findItem(R.id.password_menu_select_all).setVisible(true);
                if (passwordSelected.size() == passwordSource.size()) {
                    menu.findItem(R.id.password_menu_select_all).setIcon(R.drawable.select_all_ok);
                } else {
                    menu.findItem(R.id.password_menu_select_all).setIcon(R.drawable.select_all);
                }
                menu.findItem(R.id.password_menu_back).setVisible(true);
                menu.findItem(R.id.password_menu_add).setVisible(false);
                menu.findItem(R.id.password_menu_search).setVisible(false);
                menu.findItem(R.id.password_menu_sort).setVisible(false);
                menu.findItem(R.id.password_menu_import).setVisible(false);
                menu.findItem(R.id.password_menu_export).setVisible(false);
            } else {
                menu.findItem(R.id.password_menu_delete).setVisible(false);
                menu.findItem(R.id.password_menu_select_all).setVisible(false);
                menu.findItem(R.id.password_menu_back).setVisible(false);
                menu.findItem(R.id.password_menu_add).setVisible(true);
                menu.findItem(R.id.password_menu_search).setVisible(true);
                menu.findItem(R.id.password_menu_sort).setVisible(true);
                menu.findItem(R.id.password_menu_import).setVisible(true);
                menu.findItem(R.id.password_menu_export).setVisible(true);
                MenuItem temp;
                switch (StaticData.getInstance().passwordSearchOptions) {
                    case 1:
                        temp = menu.findItem(R.id.password_menu_search_1);
                        break;
                    case 2:
                        temp = menu.findItem(R.id.password_menu_search_2);
                        break;
                    case 3:
                        temp = menu.findItem(R.id.password_menu_search_3);
                        break;
                    case 4:
                        temp = menu.findItem(R.id.password_menu_search_4);
                        break;
                    case 5:
                        temp = menu.findItem(R.id.password_menu_search_5);
                        break;
                    default:
                        temp = menu.findItem(R.id.password_menu_search_6);
                        break;
                }
                temp.setIcon(R.drawable.radio_check);
                switch (StaticData.getInstance().passwordSortOptions) {
                    case 1:
                        temp = menu.findItem(R.id.password_menu_sort_1);
                        break;
                    case 2:
                        temp = menu.findItem(R.id.password_menu_sort_2);
                        break;
                    case 3:
                        temp = menu.findItem(R.id.password_menu_sort_3);
                        break;
                    default:
                        temp = menu.findItem(R.id.password_menu_sort_4);
                        break;
                }
                temp.setIcon(StaticData.getInstance().passwordSortOptionsUp ? R.drawable.up : R.drawable.down);

            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Logger.e("PasswordFragment", "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        Logger.e("PasswordFragment", "onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        Logger.e("PasswordFragment", "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Logger.e("PasswordFragment", "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Logger.e("PasswordFragment", "onStop");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Logger.e("PasswordFragment", "onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Logger.e("PasswordFragment", "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Logger.e("PasswordFragment", "onDetach");
        super.onDetach();
    }
}


