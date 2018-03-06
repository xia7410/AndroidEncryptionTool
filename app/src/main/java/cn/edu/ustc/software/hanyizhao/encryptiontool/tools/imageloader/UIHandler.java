package cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

/**
 * Created by HanYizhao on 2015/12/10.
 * UIHandler
 */
public class UIHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
        UIHandlerData data = (UIHandlerData) msg.obj;
        if (data.imageView.getTag().toString().equals(data.path)) {
            data.imageView.setImageBitmap(data.bitmap);
        }
    }

    class UIHandlerData {
        public String path;
        public Bitmap bitmap;
        public ImageView imageView;
    }
}
