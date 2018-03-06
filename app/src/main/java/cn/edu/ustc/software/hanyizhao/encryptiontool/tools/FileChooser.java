package cn.edu.ustc.software.hanyizhao.encryptiontool.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import cn.edu.ustc.software.hanyizhao.encryptiontool.R;

public class FileChooser implements OnClickListener {

    selectOneFileResult result = null;
    AlertDialog alog = null;

    FileMode mode = null;

    FileChooserAdapter adapter = null;

    /**
     * 弹出选择文件对话框
     *
     * @param context 上下文
     * @param filters 过滤器
     * @param title   对话框题头
     * @param result  当选择文件结束的时候的回调函数
     * @param mode    模式
     * @return
     */
    public void getFilePath(Context context, String[] filters, String title,
                            selectOneFileResult result, FileMode mode) {
        this.result = result;
        this.mode = mode;
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.file_chooser, null);
        TextView label = (TextView) layout
                .findViewById(R.id.file_chooser_title);
        ListView lv = (ListView) layout
                .findViewById(R.id.file_chooser_listView);
        FileChooserAdapter adapter = new FileChooserAdapter(context, filters,
                Environment.getExternalStorageDirectory(), label, lv);
        this.adapter = adapter;
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(adapter);

        Builder b = new AlertDialog.Builder(context).setView(layout)
                .setTitle(getDialogTitle(title, filters))
                .setNegativeButton(R.string.Cancel, null);
        if (mode.compareTo(FileMode.ONEDICTIONARY) == 0) {
            b.setPositiveButton(R.string.OK, this);
        }
        alog = b.show();
    }

    private String getDialogTitle(String title, String[] filters) {
        StringBuilder sb = new StringBuilder();
        sb.append(title);
        if (filters.length != 0) {
            sb.append("（");
            for (String i : filters) {
                sb.append("*");
                sb.append(i);
                sb.append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append("）");
        }
        return sb.toString();
    }

    class FileChooserAdapter extends BaseAdapter implements OnItemClickListener {

        List<FileInfo> data = new ArrayList<>();

        LayoutInflater la = null;
        TextView listTitle = null;
        String[] filter = null;
        ListView listView = null;

        public FileChooserAdapter(Context c, String[] filters, File root,
                                  TextView v, ListView listView) {
            la = LayoutInflater.from(c);
            this.filter = new String[filters.length];
            for (int i = 0; i < filters.length; i++) {
                this.filter[i] = filters[i].toLowerCase(Locale.CHINA);
            }
            listTitle = v;
            this.listView = listView;
            RefreshDic(root);
        }

        /**
         * 如果root是目录，则点开目录
         *
         * @param root
         */
        private void RefreshDic(File root) {
            if (root.isDirectory()) {
                data.clear();
                listTitle.setText(root.getAbsolutePath());
                listTitle.setTag(root);
                File parent = root.getParentFile();
                if (parent != null) {
                    data.add(new FileInfo(true, true, parent));
                }
                File[] list = root.listFiles();
                if (list != null) {
                    Arrays.sort(list, new Comparator<File>() {
                        @Override
                        public int compare(File lhs, File rhs) {
                            if (lhs.isDirectory() == !rhs.isDirectory()) {
                                if (lhs.isDirectory()) {
                                    return 1;
                                } else {
                                    return -1;
                                }
                            } else {
                                return lhs.getName().compareTo(rhs.getName());
                            }
                        }
                    });
                    for (File i : list) {
                        if (i.isDirectory()) {
                            data.add(new FileInfo(false, i.isDirectory(), i));
                        } else {
                            if (FileMode.ONEFILE.compareTo(mode) == 0) {
                                boolean flag = false;
                                String temp = i.getName().toLowerCase(Locale.CHINA);
                                for (String s : filter) {
                                    if (temp.endsWith(s)) {
                                        flag = true;
                                        break;
                                    }
                                }
                                if (flag) {
                                    data.add(new FileInfo(false, i
                                            .isDirectory(), i));
                                }
                            }
                        }
                    }
                }
            }
        }

        @Override
        public int getCount() {
            return data.size();
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
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = la.inflate(R.layout.file_chooser_list_view, null);
                holder.image = (ImageView) convertView
                        .findViewById(R.id.file_chooser_listView_image);
                holder.textView = (TextView) convertView
                        .findViewById(R.id.file_chooser_listView_file);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            FileInfo fi = data.get(position);
            holder.file = fi;
            if (fi.isBack) {
                holder.image.setImageResource(R.drawable.back2);
            } else if (fi.isDictionary) {
                holder.image.setImageResource(R.drawable.folder);
            } else {
                holder.image.setImageResource(R.drawable.file);
            }
            if (fi.isBack) {
                holder.textView.setText(R.string.back_last_level);
            } else {
                holder.textView.setText(fi.file.getName());
            }
            return convertView;
        }

        class ViewHolder {
            ImageView image = null;
            TextView textView = null;
            FileInfo file = null;
        }

        class FileInfo {
            boolean isBack = false;
            boolean isDictionary = false;
            File file = null;

            public FileInfo(boolean isBack, boolean isDictionary, File file) {
                this.isBack = isBack;
                this.isDictionary = isDictionary;
                this.file = file;
            }
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            FileInfo f = ((ViewHolder) view.getTag()).file;
            if (f.isDictionary) {
                RefreshDic(f.file);
                this.notifyDataSetChanged();
                listView.setSelectionFromTop(0, 0);
            } else {
                result.selectOneFile(f.file);
                alog.dismiss();
            }

        }

    }

    public interface selectOneFileResult {
        void selectOneFile(File f);
    }

    /**
     * ONEFIle:只选择一个文件；ONEDICTIONARY：只选择一个目录
     *
     * @author HanYizhao
     */
    public enum FileMode {
        ONEFILE, ONEDICTIONARY
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            result.selectOneFile((File) adapter.listTitle.getTag());
        }
    }

}
