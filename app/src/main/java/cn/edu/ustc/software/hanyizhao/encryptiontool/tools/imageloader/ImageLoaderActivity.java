package cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.edu.ustc.software.hanyizhao.encryptiontool.R;
import cn.edu.ustc.software.hanyizhao.encryptiontool.service.StaticData;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean.FolderBean;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean.MediaPath;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean.MyImageFilter;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean.MyVideoFilter;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean.TaskType;

public class ImageLoaderActivity extends AppCompatActivity implements PopupWindowListDir.OnPopupWindowListDirSelectListener, GridViewAdapter.OnSelectedListener {

    private GridView gridView;
    private RelativeLayout bottomLayout;
    private TextView mDirName;
    private TextView mDirCount;
    private ProgressDialog mProgressDialog;
    private GridViewAdapter adapter;
    private PopupWindowListDir mPopupWindowListDir;
    private Set<MediaPath> mSelected = new HashSet<>();

    private boolean hasVideo = false;

    List<FolderBean> l = new ArrayList<>();
    ImageLoaderHandler imageLoaderHandler;

    private static int compareLong(long x, long y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    public void hideProgressDialog() {
        mProgressDialog.dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hasVideo = getIntent().getBooleanExtra("all", false);
        if (hasVideo) {
            setTitle(getString(R.string.image_and_video));
        } else {
            setTitle(getString(R.string.all_images));
        }
        setContentView(R.layout.activity_image_loader);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        initViews();
        initDatas();
        initEvents();
    }

    private void initEvents() {
        bottomLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPopupWindowListDir != null) {
                    mPopupWindowListDir.showAsDropDown(bottomLayout, 0, 0);
                    lightOff();
                }
            }
        });
    }


    public void initPopupWindow() {
        mPopupWindowListDir = new PopupWindowListDir(this, l);
        mPopupWindowListDir.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                lightOn();
            }
        });
        mPopupWindowListDir.setOnPopupWindowListDirSelectListener(this);
    }

    private void lightOn() {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.alpha = 1.0f;
        getWindow().setAttributes(layoutParams);

    }

    private void lightOff() {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.alpha = 0.3f;
        getWindow().setAttributes(layoutParams);
    }


    public void hasNoData() {
        StaticData.getInstance().showMessage(
                hasVideo ? getString(R.string.no_image_and_video) :
                        getString(R.string.no_images),
                this, Toast.LENGTH_LONG);

    }

    public void initAdapter(int position) {
        adapter = new GridViewAdapter(this, l.get(position), mSelected);
        gridView.setAdapter(adapter);
        adapter.setOnSelectedListener(this);
        mDirName.setText(l.get(position).folderName);
        mDirCount.setText(l.get(position).files.size() + "张");
    }

    @Override
    public void onSelected(MediaPath mediaPath) {
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onRemove(MediaPath mediaPath) {
        supportInvalidateOptionsMenu();
    }

    private class SortHelp {
        int modifyTime;
        MediaPath mediaPath;


        public SortHelp(int modifyTime, String path, TaskType type, String duration) {
            this.modifyTime = modifyTime;
            this.mediaPath = new MediaPath(type, path, duration);
        }


    }

    private void initDatas() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "当前存储卡不可用", Toast.LENGTH_SHORT);
            return;
        }
        final List<SortHelp> sortHelps = new ArrayList<>(150);
        mProgressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.loading___));
        FolderBean recentDir = new FolderBean();
        if (hasVideo) {
            recentDir.folderName = getResources().getString(R.string.image_and_video);
        } else {
            recentDir.folderName = getResources().getString(R.string.all_images);
        }
        recentDir.briefMode = false;
        l.add(recentDir);
        if (hasVideo) {
            FolderBean videoBean = new FolderBean();
            videoBean.folderName = getResources().getString(R.string.all_videos);
            videoBean.briefMode = false;
            l.add(videoBean);
        }
        imageLoaderHandler = new ImageLoaderHandler(this);
        new Thread() {
            @Override
            public void run() {
                Set<String> imageFolders = new HashSet<>();
                Set<String> videoFolders = new HashSet<>();
                List<String> imageFolderList = new ArrayList<String>();
                List<String> videoFolderList = new ArrayList<String>();
                Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new String[]{
                                MediaStore.Images.Media.DATA,},
                        null,
                        null,
                        null);
                if (cursor != null) {
                    try {
                        while (cursor.moveToNext()) {
                            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                            String parent = path.substring(0, path.lastIndexOf('/'));
                            if (!imageFolders.contains(parent)) {
                                imageFolders.add(parent);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        cursor.close();
                    }
                    fromSetToList(imageFolders, imageFolderList);
                    MyImageFilter imageFilter = new MyImageFilter();
                    //遍历所有文件夹
                    for (String i : imageFolderList) {
                        File nowFile = new File(i);
                        File[] files = nowFile.listFiles(imageFilter);
                        Arrays.sort(files, new Comparator<File>() {
                            @Override
                            public int compare(File lhs, File rhs) {
                                return compareLong(rhs.lastModified(), lhs.lastModified());
                            }
                        });
                        if (files.length > 0) {
                            FolderBean folderBean = new FolderBean();
                            folderBean.briefMode = true;
                            folderBean.folder = nowFile;
                            folderBean.folderName = nowFile.getName();
                            for (File f : files) {
                                MediaPath mediaPath = new MediaPath(TaskType.IMAGE, f.getName());
                                folderBean.files.add(mediaPath);
                                sortHelps.add(new SortHelp((int) (f.lastModified() / 1000), f.getAbsolutePath(), TaskType.IMAGE, null));
                            }
                            l.add(folderBean);
                        }
                    }
                    if (hasVideo) {
                        cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                new String[]{
                                        MediaStore.Video.Media.DATA,},
                                null,
                                null,
                                null);
                        if (cursor != null) {
                            try {
                                while (cursor.moveToNext()) {
                                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                                    String parent = path.substring(0, path.lastIndexOf('/'));
                                    if (!videoFolders.contains(parent)) {
                                        videoFolders.add(parent);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                cursor.close();
                            }
                        }
                        fromSetToList(videoFolders, videoFolderList);
                        MyVideoFilter videoFilter = new MyVideoFilter();
                        //遍历所有文件夹
                        for (String i : videoFolderList) {
                            File nowFile = new File(i);
                            File[] files = nowFile.listFiles(videoFilter);
                            Arrays.sort(files, new Comparator<File>() {
                                @Override
                                public int compare(File lhs, File rhs) {
                                    return compareLong(rhs.lastModified(), lhs.lastModified());
                                }
                            });
                            if (files.length > 0) {
                                for (File f : files) {
                                    int iDuration = 0;
                                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                                    try {
                                        mmr.setDataSource(f.getAbsolutePath());
                                        iDuration = Integer.valueOf(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    StringBuilder sb = new StringBuilder();
                                    if (iDuration >= 3600) {
                                        sb.append(iDuration / 3600);
                                        sb.append(":");
                                        iDuration = iDuration % 3600;
                                    }
                                    if (iDuration > 60) {
                                        sb.append(String.format("%02d", iDuration / 60));
                                        sb.append(":");
                                        iDuration = iDuration % 60;
                                    } else {
                                        sb.append("00:");
                                    }
                                    sb.append(String.format("%02d", iDuration));
                                    MediaPath mediaPath = new MediaPath(TaskType.VIDEO, f.getAbsolutePath(), sb.toString());
                                    l.get(1).files.add(mediaPath);
                                    sortHelps.add(new SortHelp((int) (f.lastModified() / 1000), f.getAbsolutePath(), TaskType.VIDEO, sb.toString()));
                                }
                            }
                        }
                    }
                    Collections.sort(sortHelps, new Comparator<SortHelp>() {
                        @Override
                        public int compare(SortHelp lhs, SortHelp rhs) {
                            return compareLong(rhs.modifyTime, lhs.modifyTime);
                        }
                    });
                    for (SortHelp i : sortHelps) {
                        l.get(0).files.add(i.mediaPath);
                    }
                    //该显示的名称，比如DCIM文件夹改为相机
                    String p = Environment.getExternalStorageDirectory().getAbsolutePath().toLowerCase();
                    for (int i = hasVideo ? 2 : 1; i < l.size(); i++) {
                        FolderBean folderBean = l.get(i);
                        if (folderBean.folder != null) {
                            String tempS = folderBean.folder.getAbsolutePath().toLowerCase();
                            if (tempS.equals(p + "/ucdownloads")) {
                                folderBean.folderName = "UC下载";
                            }
                            if (tempS.equals(p + "/download")) {
                                folderBean.folderName = "下载";
                            }
                            if (tempS.equals(p + "/dcim/camera")) {
                                folderBean.folderName = "相机相册";
                            }
                            if (tempS.equals(p + "/dcim/xiaoenai")) {
                                folderBean.folderName = "小恩爱";
                            }
                            if (tempS.equals(p + "/tencent/qq_images")) {
                                folderBean.folderName = "QQ保存";
                            }
                            if (tempS.equals(p + "/tencent/qqfile_recv")) {
                                folderBean.folderName = "QQ接收";
                            }
                            if (tempS.equals(p + "/tencent/micromsg/weixin")) {
                                folderBean.folderName = "微信";
                            }
                            if (tempS.equals(p + "/tencent/qqlite_images")) {
                                folderBean.folderName = "QQ轻聊版保存";
                            }
                        }

                    }
                    if (hasVideo && l.get(1).files.size() <= 0) {
                        l.remove(1);
                    }

                    Message message = imageLoaderHandler.obtainMessage();
                    message.arg1 = ImageLoaderHandler.SCAN_FINISH;
                    message.sendToTarget();
                }
            }
        }.start();
    }

    /**
     * 将set中数据放入List中，同时去重（大小写）
     *
     * @param src
     * @param des
     */
    private void fromSetToList(Set<String> src, List<String> des) {
        for (String i : src) {
            String temp = i.toLowerCase();
            if (i.length() > 0) {
                File f = new File(i);
                if (f.isDirectory() && f.exists() && f.canRead()) {
                    boolean flag = false;
                    for (int ll = 0; ll < des.size(); ll++) {
                        if (des.get(ll).toLowerCase().equals(temp)) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        des.add(i);
                    }
                }
            }
        }
    }

    private void initViews() {
        mDirName = (TextView) findViewById(R.id.select_image_dir_name);
        mDirCount = (TextView) findViewById(R.id.select_image_dir_count);
        gridView = (GridView) findViewById(R.id.select_image_grid_view);
        bottomLayout = (RelativeLayout) findViewById(R.id.select_image_bottom_layout);

    }


    @Override
    public void OnSelected(int position) {
        mPopupWindowListDir.dismiss();
        setTitle(l.get(position).folderName);
        //ImageLoader.getInstance().clearWaitingTasks();
        initAdapter(position);
    }

    @Override
    protected void onDestroy() {
        //ImageLoader.getInstance().setShouldStop();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_image_loader_send:

                Intent i = new Intent();
                Bundle bundle = new Bundle();
                MediaPath[] paths = new MediaPath[mSelected.size()];
                mSelected.toArray(paths);
                bundle.putParcelableArray("selected", paths);
                i.putExtra("bundle", bundle);
                setResult(RESULT_OK, i);
                finish();
                break;
            default:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_loader, menu);
        MenuItem sendItem = menu.findItem(R.id.menu_image_loader_send);
        if (mSelected.size() > 0) {
            sendItem.setEnabled(true);
            sendItem.setTitleCondensed(getResources().getString(R.string.OK) + "(" + mSelected.size() + ")");
        } else {
            sendItem.setTitleCondensed(getResources().getString(R.string.OK));
            sendItem.setEnabled(false);
        }
        menu.findItem(R.id.menu_image_loader_send).setTitle("发送");
        return true;
    }


}
