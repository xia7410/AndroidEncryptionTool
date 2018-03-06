package cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader;

import android.graphics.Bitmap;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import cn.edu.ustc.software.hanyizhao.encryptiontool.service.StaticData;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.Logger;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.UIHandler.UIHandlerData;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean.ImageTools;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean.TaskType;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

/**
 * Created by HanYizhao on 2015/12/10.
 * 图片加载器
 */
public class ImageLoader {
    private static ImageLoader mImageLoader = null;

    /**
     * 图片缓冲
     */
    private LruCache<String, Bitmap> mLruCache;

    private DoThread doThread = new DoThread();
    private Boolean isWaiting = false;
    private Object a = new Object();
    private boolean shouldStop = false;

    private UIHandler uiHandler = null;

    /**
     * 图片生成任务列表
     */
    private LinkedList<OneTask> tasks = new LinkedList<>();

    private ImageLoader() {
        int memorySize = (int) (Runtime.getRuntime().maxMemory() / 4);
        Logger.e("LRUCacheSize", memorySize + "");
        mLruCache = new LruCache<String, Bitmap>(memorySize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
        doThread.start();
    }

    /**
     * UI线程调用的函数。作用是请求加载图片。
     *
     * @param path
     * @param imageView
     * @param no_photo_id
     */
    public void loadImage(String path, ImageView imageView, Integer no_photo_id, TaskType taskType) {
        if (uiHandler == null) {
            uiHandler = new UIHandler();
        }
        imageView.setTag(path);
        Bitmap temp = mLruCache.get(path);
        if (temp != null) {
            imageView.setImageBitmap(temp);
        } else {
            if (no_photo_id != null)
                imageView.setImageResource(no_photo_id);
            else
                imageView.setImageBitmap(null);
            OneTask oneTask = new OneTask();
            oneTask.imgView = new WeakReference<ImageView>(imageView);
            oneTask.Path = path;
            oneTask.taskType = taskType;
            synchronized (tasks) {
                tasks.push(oneTask);
            }
            synchronized (a) {
                if (isWaiting) {
                    a.notify();
                }
            }
        }
    }

    public static ImageLoader getInstance() {
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader();
        }
        return mImageLoader;
    }


    private class OneTask {
        public WeakReference<ImageView> imgView;
        public String Path;
        public TaskType taskType;

        public void doTheTask() {
            ImageView imageView2 = imgView.get();
            if (imageView2 != null && imageView2.getTag().toString().equals(Path)) {
                Bitmap bitmap = null;
                switch (taskType) {
                    case IMAGE:
                        bitmap = ImageTools.getThumbnail(Path, imageView2.getLayoutParams().width);
                        break;
                    case VIDEO:
                        bitmap = ImageTools.getVideoImage(Path, imageView2.getLayoutParams().width);
                        break;
                    case IMAGE_DB_VIDEO: {
                        bitmap = StaticData.getInstance().getTh(Integer.valueOf(Path.substring(5)), true);
                        break;
                    }
                    case IMAGE_DB_IMAGE: {
                        bitmap = StaticData.getInstance().getTh(Integer.valueOf(Path.substring(5)), false);
                        break;
                    }
                }
                if (bitmap != null) {
                    mLruCache.put(Path, bitmap);
                    Message message = Message.obtain();
                    UIHandlerData data = uiHandler.new UIHandlerData();
                    data.bitmap = bitmap;
                    data.imageView = imageView2;
                    data.path = Path;
                    message.obj = data;
                    uiHandler.sendMessage(message);
                }
            }
        }

    }


    /**
     * 进行图片处理的线程
     */
    private class DoThread extends Thread {
        @Override
        public void run() {
            Logger.e("ImageLoader线程", "开启：ID " + Thread.currentThread().getId());
            while (!shouldStop) {
                OneTask t;
                synchronized (tasks) {
                    if (tasks.size() == 0) {
                        t = null;
                    } else {
                        t = tasks.removeLast();
                    }
                }
                if (t == null) {
                    synchronized (a) {
                        isWaiting = true;
                        try {
                            a.wait();
                            isWaiting = false;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    t.doTheTask();
                }
            }
            mLruCache.evictAll();
            Logger.e("ImageLoader线程", "结束：ID " + Thread.currentThread().getId());
        }
    }

    public int clearWaitingTasks() {
        int result;
        synchronized (tasks) {
            result = tasks.size();
            tasks.clear();
        }
        return result;
    }


    /**
     * 关闭
     */
    public void setShouldStop() {
        this.shouldStop = true;
        synchronized (a) {
            if (isWaiting) {
                a.notify();
            }
        }
        mImageLoader = null;
    }
}
