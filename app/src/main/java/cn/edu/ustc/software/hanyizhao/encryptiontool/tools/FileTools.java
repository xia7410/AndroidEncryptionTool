package cn.edu.ustc.software.hanyizhao.encryptiontool.tools;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;

import cn.edu.ustc.software.hanyizhao.encryptiontool.service.StaticData;

/**
 * Created by HanYizhao on 2015/12/25.
 * 加密文件 解密文件
 */
public class FileTools {

    // 长度为160个字节
    private static final byte[] hasEncoded =
            new AESTools("hasAlreadyEncoded".getBytes()).Encrypt(new byte[159]);

    private static String SDCardFolderPath = null;

    /**
     * 获取SD卡中存储加密信息的文件夹名称
     *
     * @param context
     * @return
     */
    public static String getSDCardFolderPath(Context context) {
        if (SDCardFolderPath == null) {
            SDCardFolderPath = context.getApplicationInfo().packageName;
        }
        return SDCardFolderPath;
    }

    public static Boolean MoveFile(String src, String des) {
        File f = new File(src);
        File f2 = new File(des);
        if (f.exists() && f.isFile() && f.canWrite() && f.canRead()) {
            if (f2.exists()) {
                if (!f2.delete()) {
                    return false;
                }
            }
            if (f2.getParentFile() != null && !f2.getParentFile().exists()) {
                if (!f2.getParentFile().mkdirs()) {
                    return false;
                }
            }
            if (f.renameTo(f2)) {
                return true;
            }
        }
        return false;
    }

    public static Boolean decrypt(File file, String password) {
        if (file == null || password == null) {
            return false;
        }
        if (!file.exists() || !file.canWrite() || !file.canRead() || !file.isFile()) {
            return false;
        }
        //文件长度不能小于5KB
        long fileLength = file.length();
        if (fileLength < 5 * 1024) {
            return false;
        }
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "rw");
            // 通过读取文件最后160个字节判断是否已加密
            byte[] state = new byte[160];
            raf.seek(fileLength - 160);
            raf.read(state);
            if (!byteEqual(state, hasEncoded)) {
                return true;
            }
            // 计算解密密匙
            AESTools tools = new AESTools(Secret.SHAPassword(password +
                    StaticData.finalMixPassword2).getBytes());
            // 计算解密密匙的SHA
            byte[] md = Secret.SHA((Secret.SHAPassword(password +
                    StaticData.finalMixPassword2) + StaticData.finalMixPasswordString).getBytes());
            // 读取文件头，判断用户是否正确
            raf.seek(0);
            byte[] tempMd = new byte[20];
            raf.read(tempMd);
            // 判断用户是否正确
            if (!byteEqual(tempMd, md)) {
                return false;
            }
            // 读取密文
            byte[] data = new byte[1200];
            raf.seek(fileLength - 160 - 1200);
            raf.read(data);
            // 进行AES解密
            byte[] decode = tools.Decrypt(data);
            if (decode == null || decode.length != 1184) {
                throw new Exception("解密算法出现错误，未修改文件");
            }
            // 将原文写回
            raf.seek(0);
            raf.write(decode);
            FileChannel fc = raf.getChannel();
            fc.truncate(fileLength - 160 - 1200);
            fc.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Boolean encrypt(File file, String password) {
        if (file == null || password == null) {
            return false;
        }
        if (!file.exists() || !file.canWrite() || !file.canRead() || !file.isFile()) {
            return false;
        }
        //文件长度不能小于5KB
        long fileLength = file.length();
        if (fileLength < 5 * 1024) {
            return false;
        }
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "rw");
            // 通过读取文件最后160个字节判断是否已加密
            raf.seek(fileLength - 160);
            byte[] state = new byte[160];
            raf.read(state);
            if (byteEqual(state, hasEncoded)) {
                return true;
            }
            // 读取文件前1184字节
            // 存储原文
            byte[] data = new byte[1184];
            raf.seek(0);
            raf.read(data);
            // 计算加密密匙
            AESTools tools = new AESTools(Secret.SHAPassword(password +
                    StaticData.finalMixPassword2).getBytes());
            // 进行AES加密
            byte[] encode = tools.Encrypt(data);
            // 计算加密密匙的SHA值，用于标识文件
            byte[] md = Secret.SHA((Secret.SHAPassword(password +
                    StaticData.finalMixPassword2) + StaticData.finalMixPasswordString).getBytes());
            if (encode == null) {
                throw new Exception("文件加密算法出错，文件未修改");
            }
            // 写入密码的两次SHA值，用于标识创建者
            raf.seek(0);
            raf.write(md);
            raf.write(new byte[1184 - 20]);// 用0补齐空余，擦除信息
            // 写入密文 密文长度为1200
            raf.seek(fileLength);
            raf.write(encode);
            raf.write(hasEncoded);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 判断两个字节数组长度是否相等
     *
     * @param b1 数组1
     * @param b2 数组2
     * @return 是否相等
     */
    public static boolean byteEqual(byte[] b1, byte[] b2) {
        if (b1 == null || b2 == null || b1.length != b2.length) {
            return false;
        }
        for (int i = 0; i < b1.length; i++) {
            if (b1[i] != b2[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 根据文件大小获得描述
     *
     * @param size
     * @return
     */
    public static String fromSizeToString(long size) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        StringBuilder sb = new StringBuilder();
        if (size / (1024 * 1024 * 1024) != 0) {
            sb.append(decimalFormat.format(((double) size) / (1024 * 1024 * 1024)));
            sb.append(" GB");
        } else if (size / (1024 * 1024) != 0) {
            sb.append(decimalFormat.format(((double) size) / (1024 * 1024)));
            sb.append(" MB");
        } else if (size / 1024 != 0) {
            sb.append(decimalFormat.format(((double) size) / (1024)));
            sb.append(" KB");
        } else {
            sb.append(size);
            sb.append(" B");
        }
        return sb.toString();
    }
}
