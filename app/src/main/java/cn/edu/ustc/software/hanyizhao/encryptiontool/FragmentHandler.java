package cn.edu.ustc.software.hanyizhao.encryptiontool;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by HanYizhao on 2015/12/24.
 * Handler
 */
public class FragmentHandler extends Handler {

    public interface OnAddFinishListener {
        void onAddFinish();
    }

    public interface OnLoadingFinishListener {
        void onLoadingFinish();
    }

    public interface OnModifyFinishListener {
        void onModifyFinish(boolean result);
    }

    private WeakReference<OnAddFinishListener> onAddFinishListenerWeakReference = null;
    private WeakReference<OnLoadingFinishListener> onLoadingFinishListenerWeakReference = null;
    private WeakReference<OnModifyFinishListener> onModifyFinishListenerWeakReference = null;

    public void setOnAddVideoFinishListener(OnAddFinishListener listener) {
        onAddFinishListenerWeakReference = new WeakReference<OnAddFinishListener>(listener);
    }

    public void setOnLoadingFinishListener(OnLoadingFinishListener listener) {
        this.onLoadingFinishListenerWeakReference = new WeakReference<OnLoadingFinishListener>(listener);
    }

    public void setOnModifyFinishListener(OnModifyFinishListener listener) {
        this.onModifyFinishListenerWeakReference = new WeakReference<OnModifyFinishListener>(listener);
    }

    public static final int MESSAGE_ADD_FINISH = 0x01;
    public static final int MESSAGE_LOADING_FINISH = 0x02;
    public static final int MESSAGE_MODIFY_FINISH = 0x03;

    public static final int MESSAGE_MODIFY_FINISH_SUCCESS = 0x04;
    public static final int MESSAGE_MODIFY_FINISH_FAIL = 0x05;

    @Override
    public void handleMessage(Message msg) {
        switch (msg.arg1) {
            case MESSAGE_ADD_FINISH: {
                //添加视频或图片完成
                if (onAddFinishListenerWeakReference != null) {
                    OnAddFinishListener listener = onAddFinishListenerWeakReference.get();
                    if (listener != null) {
                        listener.onAddFinish();
                    }
                }
                break;
            }
            case MESSAGE_LOADING_FINISH: {
                //载入视频或图片完成
                if (onLoadingFinishListenerWeakReference != null) {
                    OnLoadingFinishListener listener = onLoadingFinishListenerWeakReference.get();
                    if (listener != null) {
                        listener.onLoadingFinish();
                    }
                }
                break;
            }
            case MESSAGE_MODIFY_FINISH: {
                //修改完成
                if (onModifyFinishListenerWeakReference != null) {
                    OnModifyFinishListener listener = onModifyFinishListenerWeakReference.get();
                    if (listener != null) {
                        listener.onModifyFinish(msg.arg2 == MESSAGE_MODIFY_FINISH_SUCCESS);
                    }
                }
                break;
            }
        }
    }
}
