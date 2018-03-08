package cn.edu.ustc.software.hanyizhao.encryptiontool;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog.Builder;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.edu.ustc.software.hanyizhao.encryptiontool.bean.Image;
import cn.edu.ustc.software.hanyizhao.encryptiontool.service.StaticData;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.FileChooser;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.FileTools;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.Logger;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.ImageLoader;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.ImageLoaderActivity;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean.DisplayUtil;


/**
 * Created by hanyizhao on 15-10-16.
 */
public class ImageFragment extends android.support.v4.app.Fragment implements View.OnClickListener, FragmentHandler.OnAddFinishListener {
    public ImageFragment() {
        Logger.e("es", "Image_Fragment 构造函数" + this.toString());
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */

    private ProgressDialog progressdialog;
    private FragmentHandler handler;
    boolean isInMultiSelect = false;

    Set<Integer> selected = new HashSet<>();

    public static ImageFragment newInstance() {
        return new ImageFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    View rootView = null;
    GridView gridView = null;
    List<Image> data = new ArrayList<>();
    MyImageGridAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_image, container, false);
        gridView = (GridView) rootView.findViewById(R.id.image_grid_view);
        handler = new FragmentHandler();
        handler.setOnAddVideoFinishListener(this);
        data = StaticData.getInstance().getAllImageData();
        adapter = new MyImageGridAdapter(this.getContext(), data);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(adapter);
        gridView.setOnItemLongClickListener(adapter);
        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!((MainActivity) getActivity()).mNavigationDrawerFragment.isDrawerOpen()) {
            inflater.inflate(R.menu.video_image_menu, menu);
            if (isInMultiSelect) {
                menu.findItem(R.id.video_image_menu_back).setVisible(true);
                menu.findItem(R.id.video_menu_remove).setVisible(true);
                menu.findItem(R.id.video_menu_delete).setVisible(true);
                menu.findItem(R.id.video_image_menu_select_all).setVisible(true);
                menu.findItem(R.id.video_menu_info).setVisible(true);
                menu.findItem(R.id.video_image_menu_add).setVisible(false);
                if (selected.size() == data.size()) {
                    menu.findItem(R.id.video_image_menu_select_all).setIcon(R.drawable.select_all_ok);
                } else {
                    menu.findItem(R.id.video_image_menu_select_all).setIcon(R.drawable.select_all);
                }
            } else {
                menu.findItem(R.id.video_menu_info).setVisible(false);
                menu.findItem(R.id.video_image_menu_back).setVisible(false);
                menu.findItem(R.id.video_image_menu_select_all).setVisible(false);
                menu.findItem(R.id.video_menu_remove).setVisible(false);
                menu.findItem(R.id.video_menu_delete).setVisible(false);
                menu.findItem(R.id.video_image_menu_add).setVisible(true);
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case VideoFragment.requestCode_SELECT: {
                if (resultCode == Activity.RESULT_OK) {
                    progressdialog = ProgressDialog.show(this.getContext(), null, getString(R.string.loading___));
                    //会新开启一个线程，线程结束的时候，发送消息
                    AddVideoImage.addVideoAndImage(data, this.getContext(), handler);
                }
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void removeFiles(String newPath) {
        List<String> trueRemovedPaths = new ArrayList<>();
        List<Integer> trueRemoved = new ArrayList<>();
        for (Integer i : selected) {
            Image image = data.get(i);
            File nowFile = new File(StaticData.fromImageIdToSavePath(image.id));
            FileTools.decrypt(nowFile,
                    StaticData.getInstance().getRealPassword());
            File tempF;
            if (newPath == null) {
                tempF = new File(image.path);
            } else {
                tempF = new File(newPath, new File(image.path).getName());
            }
            //有重名，寻找没有重名的路径
            if (tempF.exists()) {
                int l = 1;
                File parent = tempF.getParentFile();
                //记录后缀
                String last = "";
                if (tempF.getName().contains(".")) {
                    last = tempF.getName().substring(tempF.getName().lastIndexOf('.'));
                }
                do {
                    tempF = new File(parent, image.name + "(" + l + ")" + last);
                    if (!tempF.exists()) {
                        break;
                    }
                    l++;
                } while (true);
            }
            if (FileTools.MoveFile(nowFile.getAbsolutePath(), tempF.getAbsolutePath())) {
                trueRemoved.add(image.id);
                trueRemovedPaths.add(image.path);
            }
        }
        for (int i : trueRemoved) {
            StaticData.getInstance().deleteImage(i);
        }
        Context context = ImageFragment.this.getContext();
        String[] paths = new String[trueRemovedPaths.size()];
        trueRemovedPaths.toArray(paths);
        AddVideoImage.ScanFile(paths, context, false);
        OutMultiSelect();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.video_image_menu_add: {
                Intent i = new Intent(this.getContext(), ImageLoaderActivity.class);
                i.putExtra("image", true);
                startActivityForResult(i, VideoFragment.requestCode_SELECT);
                break;
            }
            case R.id.video_image_menu_back: {
                OutMultiSelect();
                break;
            }
            case R.id.video_image_menu_select_all: {
                if (selected.size() == data.size()) {
                    selected.clear();
                } else {
                    for (int i = 0; i < data.size(); i++) {
                        selected.add(i);
                    }
                }
                adapter.notifyDataSetChanged();
                getActivity().supportInvalidateOptionsMenu();
                break;
            }
            case R.id.video_menu_remove: {
                Builder startB = new Builder(this.getContext());
                startB.setTitle(R.string.remove_to);
                startB.setItems(R.array.remove_file_options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Builder b = new Builder(ImageFragment.this.getContext());
                            b.setTitle(R.string.remove);
                            b.setMessage("确认移除这" + selected.size() + "个图片吗？\n（恢复至原目录）");
                            b.setNegativeButton(R.string.Cancel, null);
                            b.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    removeFiles(null);
                                }
                            });
                            b.show();
                        } else {
                            FileChooser fileChooser = new FileChooser();
                            fileChooser.getFilePath(ImageFragment.this.getContext(), new String[]{}, "",
                                    new FileChooser.selectOneFileResult() {
                                        @Override
                                        public void selectOneFile(File f) {
                                            Builder b = new Builder(ImageFragment.this.getContext());
                                            b.setTitle(R.string.remove);
                                            final String newPath = f.getAbsolutePath();
                                            b.setMessage("确认移除这" + selected.size() + "个图片吗？\n恢复至：\n" + newPath);
                                            b.setNegativeButton(R.string.Cancel, null);
                                            b.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    removeFiles(newPath);
                                                }
                                            });
                                            b.show();
                                        }
                                    }, FileChooser.FileMode.ONEDICTIONARY);
                        }
                    }
                });
                startB.show();
                break;
            }
            case R.id.video_menu_delete: {
                Builder b = new Builder(this.getContext());
                b.setTitle(R.string.delete);
                b.setMessage("确认删除这" + selected.size() + "个图片吗？\n（从内存删除，不可撤销）");
                b.setNegativeButton(R.string.Cancel, null);
                b.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<Integer> trueRemoved = new ArrayList<Integer>();
                        for (Integer i : selected) {
                            Image image = data.get(i);
                            File nowFile = new File(StaticData.fromImageIdToSavePath(image.id));
                            if (!nowFile.exists() || nowFile.delete()) {
                                trueRemoved.add(image.id);
                            }
                        }
                        for (int i : trueRemoved) {
                            StaticData.getInstance().deleteImage(i);
                        }
                        OutMultiSelect();
                    }
                });
                b.show();
                break;
            }
            case R.id.video_menu_info: {
                boolean single = (selected.size() == 1);
                AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
                LayoutInflater inflater = LayoutInflater.from(this.getContext());
                if (single) {
                    Image image = data.get(selected.iterator().next());
                    File oldFile = new File(image.path);
                    File newFile = new File(StaticData.fromImageIdToSavePath(image.id));
                    builder.setTitle(image.name);
                    View root = inflater.inflate(R.layout.dialog_video_info, null);
                    ((TextView) root.findViewById(R.id.dialog_video_info_file)).setText(oldFile.getName());
                    ((TextView) root.findViewById(R.id.dialog_video_info_location)).setText(oldFile.getParent());
                    root.findViewById(R.id.dialog_video_info_duration_row).setVisibility(View.GONE);
                    long length = image.hasDecrypted ? newFile.length() : newFile.length() - 1200 - 160;
                    String length1 = FileTools.fromSizeToString(length);
                    DecimalFormat format = new DecimalFormat("#,###");
                    String length2 = format.format(length);
                    String length3 = length1 + "（" + length2 + " 字节）";
                    ((TextView) root.findViewById(R.id.dialog_video_info_size)).setText(length3);
                    builder.setView(root);
                } else {
                    builder.setTitle(R.string.info);
                    View root = inflater.inflate(R.layout.dialog_videos_info, null);
                    long length = 0;
                    for (int i : selected) {
                        Image image = data.get(i);
                        File newFile = new File(StaticData.fromImageIdToSavePath(image.id));
                        length += image.hasDecrypted ? newFile.length() : newFile.length() - 1200 - 160;
                    }
                    String in = selected.size() + " 图片";
                    ((TextView) root.findViewById(R.id.dialog_videos_info_include)).setText(in);
                    String length1 = FileTools.fromSizeToString(length);
                    DecimalFormat format = new DecimalFormat("#,###");
                    String length2 = format.format(length);
                    String length3 = length1 + "（" + length2 + " 字节）";
                    ((TextView) root.findViewById(R.id.dialog_videos_info_all_size)).setText(length3);
                    builder.setView(root);
                }
                builder.setPositiveButton(R.string.OK, null);
                builder.show();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //((MainActivity) context).onSectionAttached(2);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onAddFinish() {
        if (progressdialog != null) {
            progressdialog.dismiss();
        }
        adapter.notifyDataSetChanged();
    }

    class MyImageGridAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

        List<Image> list;
        Context context;
        LayoutInflater inflater;

        public MyImageGridAdapter(Context context, List<Image> data) {
            this.context = context;
            this.list = data;
            this.inflater = LayoutInflater.from(context);
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(displayMetrics);
            widthPixels = (displayMetrics.widthPixels - DisplayUtil.dip2px(context, 8)) / 3;
            buttonWidthPixels = widthPixels / 6;
            buttonPaddingPixels = buttonWidthPixels / 4;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private int widthPixels = 0;
        private int buttonWidthPixels = 0;
        private int buttonPaddingPixels = 0;

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.image_grid_view, null);
                holder.imageView = (ImageView) convertView.findViewById(R.id.image_grid_view_imageView);
                holder.imageButton = ((ImageButton) convertView.findViewById(R.id.image_grid_view_image_button));
                ViewGroup.LayoutParams layoutParams = holder.imageView.getLayoutParams();
                layoutParams.width = widthPixels;
                layoutParams.height = widthPixels;
                layoutParams = holder.imageButton.getLayoutParams();
                layoutParams.width = buttonWidthPixels;
                layoutParams.height = buttonWidthPixels;
                holder.imageButton.setPadding(0, buttonPaddingPixels, buttonPaddingPixels, 0);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Image image = list.get(position);
            ImageLoader.getInstance().loadImage("image" + image.id + "", holder.imageView, null, StaticData.getInstance());
            if (isInMultiSelect) {
                holder.imageButton.setImageResource(selected.contains(position) ?
                        R.drawable.photo_selected : R.drawable.photo_no_selected);
                holder.imageButton.setVisibility(View.VISIBLE);
            } else {
                holder.imageButton.setVisibility(View.GONE);
            }
            if (isInMultiSelect && selected.contains(position)) {
                holder.imageView.setColorFilter(Color.parseColor("#77000000"));
            } else {
                holder.imageView.setColorFilter(null);
            }
            return convertView;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (isInMultiSelect) {
                if (selected.contains(position)) {
                    removePosition(position);
                } else {
                    addPosition(position);
                }
            } else {
                Image image = list.get(position);
                File file = new File(StaticData.fromImageIdToSavePath(image.id));
                if (FileTools.decrypt(file, StaticData.getInstance().getRealPassword())) {
                    image.hasDecrypted = true;
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri uri;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        uri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", file);
                    } else {
                        uri = Uri.fromFile(file);
                    }
                    intent.setDataAndType(uri, "image/*");
                    startActivity(intent);
                } else {
                    StaticData.getInstance().showMessage(getString(R.string.image_decryption_failure), this.context);
                }
            }
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (!isInMultiSelect) {
                InMultiSelect();
                addPosition(position);
            }
            return true;
        }

        class ViewHolder {
            public ImageView imageView;
            public ImageButton imageButton;
        }
    }

    /**
     * 进入选择模式
     */
    public void InMultiSelect() {
        isInMultiSelect = true;
        adapter.notifyDataSetChanged();
        getActivity().supportInvalidateOptionsMenu();
    }

    public void OutMultiSelect() {
        isInMultiSelect = false;
        selected.clear();
        adapter.notifyDataSetChanged();
        getActivity().supportInvalidateOptionsMenu();
    }

    public void addPosition(int id) {
        selected.add(id);
        adapter.notifyDataSetChanged();
        getActivity().supportInvalidateOptionsMenu();
    }

    public void removePosition(int id) {
        selected.remove(id);
        if (selected.size() == 0) {
            OutMultiSelect();
        } else {
            adapter.notifyDataSetChanged();
            getActivity().supportInvalidateOptionsMenu();
        }
    }

    public boolean onKeyBackDown() {
        if (isInMultiSelect) {
            OutMultiSelect();
            return true;
        }
        return false;
    }

}
