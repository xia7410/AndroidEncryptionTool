package cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by HanYizhao on 2015/12/10.
 */
public class ImageLoaderHandler extends Handler {

    public static final int SCAN_FINISH = 0x001;
    private WeakReference<ImageLoaderActivity> imageLoaderActivityWeakReference;

    public ImageLoaderHandler(ImageLoaderActivity i) {
        imageLoaderActivityWeakReference = new WeakReference<ImageLoaderActivity>(i);
    }

    @Override
    public void handleMessage(Message msg) {
        ImageLoaderActivity imageLoaderActivity = imageLoaderActivityWeakReference.get();
        if (imageLoaderActivity != null) {
            switch (msg.arg1) {
                case SCAN_FINISH: {
                    imageLoaderActivity.hideProgressDialog();
                    if (imageLoaderActivity.l.get(0).files.size() == 0) {
                        imageLoaderActivity.hasNoData();
                    } else {
                        imageLoaderActivity.initAdapter(0);
                        imageLoaderActivity.initPopupWindow();
                    }
                    break;
                }
            }
        }
    }
}
