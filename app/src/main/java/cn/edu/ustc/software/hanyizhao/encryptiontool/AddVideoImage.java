package cn.edu.ustc.software.hanyizhao.encryptiontool;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;

import java.io.File;

import cn.edu.ustc.software.hanyizhao.encryptiontool.service.StaticData;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.FileTools;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.Logger;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean.MediaPath;

/**
 * Created by HanYizhao on 2015/12/28.
 * 处理添加视频与图片的结果
 */
public class AddVideoImage {

    public static void ScanFile(String[] paths, Context context, boolean isVideo) {
//        for (String i : paths) {
//            ContentValues values = new ContentValues();
//            values.put(MediaStore.MediaColumns.DISPLAY_NAME, new File(i).getName());
//            if (isVideo) {
//                values.put(MediaStore.Video.Media.DATA, i);
//                context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
//            } else {
//                values.put(MediaStore.Images.Media.DATA, i);
//                context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//            }
//        }

//        for (String i : paths) {
//            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(i))));
//        }


        MediaScannerConnection.scanFile(context, paths, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                Logger.e("MediaScanner Listen", "completed " + path);
            }
        });
    }

    public static void addVideoAndImage(Intent data, final Context context, final FragmentHandler handler) {
        final Parcelable[] selected = data.getBundleExtra("bundle").getParcelableArray("selected");
        //生成一个线程，专门用来处理添加图片
        Thread thread = new Thread() {
            @Override
            public void run() {
                if (selected != null) {
                    for (Parcelable i : selected) {
                        MediaPath mp = (MediaPath) i;
                        if (!mp.isVideo) {
                            //图片
                            int id = StaticData.getInstance().addImage(mp.path);
                            if (id != -1) {
                                //数据库添加图片成功
                                File tempFile = new File(mp.path);
                                boolean flag = false;
                                String newPath = StaticData.fromImageIdToSavePath(id);
                                //尝试移动图片并加密
                                if (FileTools.MoveFile(mp.path, newPath)) {
                                    if (FileTools.encrypt(new File(newPath), StaticData.getInstance().getRealPassword())) {
                                        flag = true;
                                    } else {
                                        FileTools.MoveFile(newPath, mp.path);
                                    }
                                }
                                if (!flag) {
                                    //移动图片失败
                                    StaticData.getInstance().deleteImage(id);
                                }
                                if (flag) {
                                    Logger.e("AddImage", mp.path);
                                }
                            }
                        } else {
                            //视频
                            int id = StaticData.getInstance().addVideo(mp.path, mp.duration);
                            if (id != -1) {
                                //数据库添加视频成功
                                File tempFile = new File(mp.path);
                                boolean flag = false;
                                String newPath = StaticData.fromVideoIdToSavePath(id);
                                //尝试移动视频并加密
                                if (FileTools.MoveFile(mp.path, newPath)) {
                                    if (FileTools.encrypt(new File(newPath), StaticData.getInstance().getRealPassword())) {
                                        flag = true;
                                    } else {
                                        FileTools.MoveFile(newPath, mp.path);
                                    }
                                }
                                if (!flag) {
                                    //移动视频失败
                                    StaticData.getInstance().deleteVideo(id);
                                }
                                if (flag) {
                                    Logger.e("AddVideo", mp.path);
                                }
                            }
                        }
                    }
                }
                Message message = handler.obtainMessage();
                message.arg1 = FragmentHandler.MESSAGE_ADD_FINISH;
                message.sendToTarget();
            }
        };
        thread.start();
    }
}
