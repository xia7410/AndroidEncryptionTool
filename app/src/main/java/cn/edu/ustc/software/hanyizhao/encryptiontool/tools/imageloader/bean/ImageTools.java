package cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * Created by HanYizhao on 2015/12/24.
 * 处理图片
 * （生成视频截图）
 * （生成图片缩略图）
 */
public class ImageTools {
    /**
     * 生成视频缩略图
     *
     * @param path           视频绝对路径
     * @param recommendWidth 推荐宽度，默认0,会转为200
     * @return 图片
     */
    public static Bitmap getVideoImage(String path, int recommendWidth) {
        Bitmap b = ThumbnailUtils.createVideoThumbnail(path,
                MediaStore.Images.Thumbnails.MINI_KIND);
        if (b != null) {
            if (recommendWidth <= 0) {
                recommendWidth = 200;
            }
            Matrix matrix = new Matrix();
            float scale = ((float) recommendWidth) / b.getWidth();
            matrix.postScale(scale, scale); //长和宽放大缩小的比例
            b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
        }
        return b;
    }

    /**
     * 生成图片缩略图
     *
     * @param path           图片完整路径
     * @param recommendWidth 推荐宽度 可以为0
     * @return 图片
     */
    public static Bitmap getThumbnail(String path, int recommendWidth) {
        Bitmap result = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高
        BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false;
        //计算缩放比
        if (recommendWidth <= 0) {
            recommendWidth = 200;
        }
        int be = (int) (options.outHeight / (float) recommendWidth);
        if (be <= 0)
            be = 1;
        options.inSampleSize = be;
        //重新读入图片，注意这次要把options.inJustDecodeBounds 设为 false哦
        result = BitmapFactory.decodeFile(path, options);
        if (result != null) {
            int height = result.getHeight();
            int width = result.getWidth();
            if (height > 200 && width > 200) {
                float x = height > width ? (float) width / 200 : (float) height / 200;
                height = (int) ((float) height / x);
                width = (int) ((float) width / x);
                Bitmap temp = Bitmap.createScaledBitmap(result, width, height, false);
                if (temp != result) {
                    result.recycle();
                    result = temp;
                }
            }
            if (height != width) {
                int startX = width > height ? (width - height) / 2 : 0;
                int startY = width > height ? 0 : (height - width) / 2;
                int l = width > height ? height : width;
                Bitmap temp = Bitmap.createBitmap(result, startX, startY, l, l);
                if (temp != result) {
                    result.recycle();
                    result = temp;
                }
            }
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (exif != null) {
                int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                int degree;
                switch (ori) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                    default:
                        degree = 0;
                        break;
                }
                if (degree != 0) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(degree);
                    result = Bitmap.createBitmap(result, 0, 0, result.getWidth(), result.getHeight(), matrix, false);
                }
            }

        }
        return result;
    }

    public static String getDurationString(long milliseconds) {
        int iDuration = (int) (milliseconds / 1000);
        StringBuilder sb = new StringBuilder();
        if (iDuration >= 3600) {
            sb.append(iDuration / 3600);
            sb.append(":");
            iDuration = iDuration % 3600;
        }
        if (iDuration > 60) {
            sb.append(String.format(Locale.getDefault(), "%02d", iDuration / 60));
            sb.append(":");
            iDuration = iDuration % 60;
        } else {
            sb.append("00:");
        }
        sb.append(String.format(Locale.getDefault(), "%02d", iDuration));
        return sb.toString();
    }

    /**
     * 将图片保存在存储卡中
     *
     * @param bitmap 图片
     * @param path   路径
     * @return 结果
     */
    public static boolean saveBitmap(Bitmap bitmap, String path) {
        boolean result = false;
        if (bitmap != null && path != null) {
            File file = new File(path);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file, false);
                result = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return result;
    }

}
