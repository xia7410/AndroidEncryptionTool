package cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.Logger;

/**
 * Created by HanYizhao on 2015/12/10.
 */
public class ImageLoaderHandler extends Handler {

    public static final int SCAN_FINISH = 0x001;
    public static final int NEW_FILES = 0x002;
    private WeakReference<ImageLoaderActivity> imageLoaderActivityWeakReference;

    public ImageLoaderHandler(ImageLoaderActivity i) {
        imageLoaderActivityWeakReference = new WeakReference<>(i);
    }

    @Override
    public void handleMessage(Message msg) {
        ImageLoaderActivity imageLoaderActivity = imageLoaderActivityWeakReference.get();
        if (imageLoaderActivity != null) {
            switch (msg.what) {
                case SCAN_FINISH: {
                    imageLoaderActivity.handlerScanFinished();
                    break;
                }
                case NEW_FILES: {
                    Bundle bundle = msg.getData();
                    if (bundle != null) {
                        ArrayList<String> newFileList = bundle.getStringArrayList("value");
                        boolean[] newFilesIsVideo = bundle.getBooleanArray("isVideo");
                        if (newFileList != null && newFilesIsVideo != null
                                && newFilesIsVideo.length == newFileList.size()) {
                            Logger.e("newFile", newFileList.size() + " Files");
                            for (String i : newFileList) {
                                Logger.e("newFile", i);
                            }
                            imageLoaderActivity.handlerFindNewFiles(newFileList, newFilesIsVideo);
                        }
                    }
                    break;
                }
            }
        }
    }
}
