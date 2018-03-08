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
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.edu.ustc.software.hanyizhao.encryptiontool.R;
import cn.edu.ustc.software.hanyizhao.encryptiontool.service.StaticData;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.Logger;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean.FolderBean;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean.ImageTools;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean.MediaPath;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean.MyImageAndVideoFilter;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean.MyImageFilter;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean.MyVideoFilter;

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
    private boolean hasImage = false;

    private boolean scanWholeSDThreadShouldStop = false;

    HashSet<String> alreadyHave = new HashSet<>();
    HashMap<String, FolderBean> subFolder = new HashMap<>();
    FolderBean allFilesFolder, allVideoFolder;
    ImageLoaderHandler imageLoaderHandler;

    private class ScanWholeSDThread extends Thread {

        private FileFilter fileFilter;
        private FileFilter myVideoFilter = new MyVideoFilter();
        private FileFilter directionFileFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory() && pathname.canWrite()
                        && !pathname.isHidden();
            }
        };

        private ArrayList<String> newFiles = new ArrayList<>();
        private ArrayList<Boolean> newFilesIsVideo = new ArrayList<>();
        private long startTime;

        ScanWholeSDThread() {
            if (hasImage && hasVideo) {
                fileFilter = new MyImageAndVideoFilter();
            } else if (hasImage) {
                fileFilter = new MyImageFilter();
            } else if (hasVideo) {
                fileFilter = new MyVideoFilter();
            } else {
                fileFilter = new MyImageAndVideoFilter();
            }
        }

        private void sendMessage() {
            if (newFiles.size() > 0) {
                Message message = imageLoaderHandler.obtainMessage(ImageLoaderHandler.NEW_FILES);
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("value", newFiles);
                boolean[] bA = new boolean[newFilesIsVideo.size()];
                {
                    for (int i = 0; i < newFilesIsVideo.size(); i++) {
                        bA[i] = newFilesIsVideo.get(i);
                    }
                }
                bundle.putBooleanArray("isVideo", bA);
                message.setData(bundle);
                message.sendToTarget();
                newFiles = new ArrayList<>();
                newFilesIsVideo = new ArrayList<>();
            }
            startTime = System.currentTimeMillis();
        }

        private void scanOneFolder(File file) {
            if (file.isDirectory() && !new File(file, ".nomedia").exists() && !scanWholeSDThreadShouldStop) {
                if (System.currentTimeMillis() - startTime > 1000) {
                    sendMessage();
                }
                File[] files = file.listFiles(fileFilter);
                if (files != null) {
                    for (File i : files) {
                        String path = i.getAbsolutePath();
                        if (!alreadyHave.contains(path.toLowerCase())) {
                            newFiles.add(path);
                            newFilesIsVideo.add(myVideoFilter.accept(i));
                        }
                    }
                }
                files = file.listFiles(directionFileFilter);
                if (files != null) {
                    for (File i : files) {
                        scanOneFolder(i);
                    }
                }
            }
        }

        @Override
        public void run() {
            Logger.e("Thread Start:", "ID: " + Thread.currentThread().getId());
            try {
                startTime = System.currentTimeMillis();
                File f = Environment.getExternalStorageDirectory();
                scanOneFolder(f);
                if (!scanWholeSDThreadShouldStop) {
                    sendMessage();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Logger.e("Thread Stop:", "ID: " + Thread.currentThread().getId());
        }
    }

    public void hideProgressDialog() {
        mProgressDialog.dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        hasImage = intent.getBooleanExtra("image", false);
        hasVideo = intent.getBooleanExtra("video", false);
        if (hasVideo && hasImage) {
            setTitle(getString(R.string.image_and_video));
        } else if (hasImage) {
            setTitle(getString(R.string.all_images));
        } else if (hasVideo) {
            setTitle(getString(R.string.all_videos));
        } else {
            hasImage = true;
            hasVideo = true;
            setTitle(getString(R.string.image_and_video));
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


    private List<FolderBean> preparePopUpWindowData() {
        List<FolderBean> allFolder = new ArrayList<>();
        if (hasImage) {
            allFolder.add(allFilesFolder);
        }
        if (hasVideo) {
            allFolder.add(allVideoFolder);
        }
        if (hasImage) {
            List<FolderBean> tmp = new ArrayList<>(subFolder.values());
            Collections.sort(tmp, new Comparator<FolderBean>() {
                @Override
                public int compare(FolderBean f1, FolderBean f2) {
                    long y = 0, x = 0;
                    if (f1.files.size() > 0) {
                        y = f1.files.get(0).modify;
                    }
                    if (f2.files.size() > 0) {
                        x = f2.files.get(0).modify;
                    }
                    return (x < y) ? -1 : ((x == y) ? 0 : 1);
                }
            });
            allFolder.addAll(tmp);
        }
        return allFolder;
    }

    public void initPopupWindow() {
        List<FolderBean> allFolder = preparePopUpWindowData();
        mPopupWindowListDir = new PopupWindowListDir(this, allFolder);
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

    public void handlerFindNewFiles(List<String> newFiles, boolean[] newFilesIsVideo) {
        HashSet<FolderBean> needSortFolder = new HashSet<>();
        for (int i = 0; i < newFiles.size(); i++) {
            String filePath = newFiles.get(i);
            File file = new File(filePath);
            long lastModify = file.lastModified();
            if (newFilesIsVideo[i]) {
                long iDuration = 0;
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                try {
                    mmr.setDataSource(filePath);
                    iDuration = Integer.valueOf(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                MediaPath videoPath = new MediaPath(true, filePath,
                        lastModify, ImageTools.getDurationString(iDuration));
                allVideoFolder.files.add(videoPath);
                needSortFolder.add(allVideoFolder);
                if (hasImage) {
                    allFilesFolder.files.add(videoPath);
                    needSortFolder.add(allFilesFolder);
                }
            } else {
                MediaPath imagePath = new MediaPath(false, filePath, lastModify);
                allFilesFolder.files.add(imagePath);
                needSortFolder.add(allFilesFolder);
                String parent = file.getParent().toLowerCase();
                FolderBean target = subFolder.get(parent);
                if (target == null) {
                    target = new FolderBean();
                    target.briefMode = true;
                    target.folder = file.getParentFile();
                    target.folderName = target.folder.getName();
                    subFolder.put(parent, target);
                }
                target.files.add(new MediaPath(false, file.getName(), lastModify));
                needSortFolder.add(target);
            }
        }
        for (FolderBean i : needSortFolder) {
            Collections.sort(i.files);
        }
        mPopupWindowListDir.refreshFolderBean(preparePopUpWindowData());
        if (needSortFolder.contains(adapter.getFolderBean())) {
            adapter.notifyDataSetChanged();
            mDirCount.setText(adapter.getFolderBean().files.size() + "");
        }
    }

    public void handlerScanFinished() {
        hideProgressDialog();
        FolderBean selected = hasImage ? allFilesFolder : allVideoFolder;
        selected.isSelected = true;
        initAdapter(selected);
        initPopupWindow();
        Thread scanWholeSDThread = new ScanWholeSDThread();
        scanWholeSDThread.start();
    }

    public void initAdapter(FolderBean selected) {
        adapter = new GridViewAdapter(this, selected, mSelected);
        gridView.setAdapter(adapter);
        adapter.setOnSelectedListener(this);
        mDirName.setText(selected.folderName);
        mDirCount.setText(selected.files.size() + "");
    }

    @Override
    public void onSelected() {
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onRemove() {
        supportInvalidateOptionsMenu();
    }


    private void initDatas() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            StaticData.getInstance().showMessage("当前存储卡不可用", this);
            return;
        }
        mProgressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.loading___));
        if (hasImage) {
            allFilesFolder = new FolderBean();
            if (hasVideo) {
                allFilesFolder.folderName = getResources().getString(R.string.image_and_video);
            } else {
                allFilesFolder.folderName = getResources().getString(R.string.all_images);
            }
        }
        if (hasVideo) {
            allVideoFolder = new FolderBean();
            allVideoFolder.isVideoFolder = true;
            allVideoFolder.folderName = getResources().getString(R.string.all_videos);
        }
        imageLoaderHandler = new ImageLoaderHandler(this);
        new Thread() {
            @Override
            public void run() {
                // Get Images from MediaStore.
                if (hasImage) {
                    Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            new String[]{
                                    MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_MODIFIED},
                            null,
                            null,
                            null);
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            MediaPath mediaPath = new MediaPath(false, cursor.getString(0),
                                    cursor.getLong(1) * 1000);
                            File newFile = new File(mediaPath.path);
                            if (newFile.exists()) {
                                alreadyHave.add(mediaPath.path.toLowerCase());
                                allFilesFolder.files.add(mediaPath);
                                String parent = newFile.getParent().toLowerCase();
                                FolderBean target = subFolder.get(parent);
                                if (target == null) {
                                    target = new FolderBean();
                                    target.briefMode = true;
                                    target.folder = newFile.getParentFile();
                                    target.folderName = target.folder.getName();
                                    subFolder.put(parent, target);
                                }
                                target.files.add(new MediaPath(false, newFile.getName(), mediaPath.modify));
                            }
                        }
                        cursor.close();
                    }
                }
                // Get Images from MediaStore.
                if (hasVideo) {
                    Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            new String[]{
                                    MediaStore.Video.Media.DATA, MediaStore.Video.Media.DATE_MODIFIED, MediaStore.Video.Media.DURATION},
                            null,
                            null,
                            null);
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            MediaPath mediaPath = new MediaPath(true, cursor.getString(0),
                                    cursor.getLong(1) * 1000,
                                    ImageTools.getDurationString(cursor.getLong(2)));
                            if (new File(mediaPath.path).exists()) {
                                alreadyHave.add(mediaPath.path.toLowerCase());
                                if (hasImage) {
                                    allFilesFolder.files.add(mediaPath);
                                }
                                allVideoFolder.files.add(mediaPath);
                            }
                        }
                        cursor.close();
                    }
                }
                // Sort Images.
                if (hasImage) {
                    Collections.sort(allFilesFolder.files);
                    for (FolderBean i : subFolder.values()) {
                        Collections.sort(i.files);
                    }
                }
                // Sort Videos.
                if (hasVideo) {
                    Collections.sort(allVideoFolder.files);
                }
                Message message = imageLoaderHandler.obtainMessage(ImageLoaderHandler.SCAN_FINISH);
                message.sendToTarget();
            }
        }.start();
    }

    private void initViews() {
        mDirName = (TextView) findViewById(R.id.select_image_dir_name);
        mDirCount = (TextView) findViewById(R.id.select_image_dir_count);
        gridView = (GridView) findViewById(R.id.select_image_grid_view);
        bottomLayout = (RelativeLayout) findViewById(R.id.select_image_bottom_layout);
    }


    @Override
    public void OnSelected(FolderBean selected) {
        mPopupWindowListDir.dismiss();
        setTitle(selected.folderName);
        //ImageLoader.getInstance().clearWaitingTasks();
        initAdapter(selected);
    }

    @Override
    protected void onDestroy() {
        //ImageLoader.getInstance().setShouldStop();
        scanWholeSDThreadShouldStop = true;
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
