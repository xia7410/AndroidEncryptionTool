package cn.edu.ustc.software.hanyizhao.encryptiontool.tools;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.edu.ustc.software.hanyizhao.encryptiontool.bean.Image;
import cn.edu.ustc.software.hanyizhao.encryptiontool.bean.Password;
import cn.edu.ustc.software.hanyizhao.encryptiontool.bean.Video;
import cn.edu.ustc.software.hanyizhao.encryptiontool.service.StaticData;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean.ImageTools;

/**
 * Created by HanYizhao on 2015/9/22.
 * 数据库操作
 */
public class DBTools {
    static String defaultCharset = "UTF-8";

    private SQLiteDatabase sQLiteDatabase = null;

    private static DBTools dbTools = null;

    private DBTools() {
    }

    public static DBTools getInstance() {
        if (dbTools == null) {
            dbTools = new DBTools();
        }
        return dbTools;
    }

    /**
     * 测试外置存储卡是否存在
     *
     * @return
     */
    public File testSDCardFile() {
        File temp = Environment.getExternalStorageDirectory();
        if (temp != null && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return temp;
        }
        return null;
    }

    /**
     * 将image表中一条数据删除
     *
     * @param id
     * @return
     */
    public boolean deleteImage(int id) {
        SQLiteDatabase db = getConnection();
        if (db == null) {
            return false;
        }
        db.delete("image", "id = ?", new String[]{id + ""});
        return true;
    }

    /**
     * 将video表中一条数据删除
     *
     * @param id
     * @return
     */
    public boolean deleteVideo(int id) {
        SQLiteDatabase db = getConnection();
        if (db == null) {
            return false;
        }
        db.delete("video", "id = ?", new String[]{id + ""});
        return true;
    }


    /**
     * 将password表中一条数据删除
     *
     * @param id
     * @return
     */
    public boolean deletePassword(int id) {
        SQLiteDatabase db = getConnection();
        if (db == null) {
            return false;
        }
        db.delete("password", "id = ?", new String[]{id + ""});
        return true;
    }

    /**
     * 修改password中的一条，按照id
     *
     * @param p
     * @return
     */
    public boolean setPassword(Password p, String realPassword, boolean byTimeOrID) {
        boolean result = false;
        SQLiteDatabase db = getConnection();
        if (db == null || realPassword == null) {
            return result;
        }
        ContentValues cv = new ContentValues();
        AESTools t = null;
        try {
            t = new AESTools(Secret.SHAPassword(realPassword +
                    StaticData.finalMixPassword2).getBytes(defaultCharset));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            cv.put("name", Secret.bytesToHexString(t.Encrypt(p.name
                    .getBytes(defaultCharset))));
            cv.put("username", Secret.bytesToHexString(t.Encrypt(p.userName
                    .getBytes(defaultCharset))));
            cv.put("password", Secret.bytesToHexString(t.Encrypt(p.password
                    .getBytes(defaultCharset))));
            cv.put("tips", Secret.bytesToHexString(t.Encrypt(p.tip
                    .getBytes(defaultCharset))));
            cv.put("lasttime", p.lastTime);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        int count;
        if (byTimeOrID) {
            count = db.update("password", cv, "time = ?", new String[]{p.time});
        } else {
            count = db.update("password", cv, "id = ?", new String[]{p.id
                    + ""});
        }
        if (count > 0) {
            result = true;
        }
        return result;
    }

    /**
     * 修改登录密码
     *
     * @param newPassword
     * @param data
     * @return
     */
    public boolean modifyLoginPassword(String oldPassword, String newPassword,
                                       List<Password> data, List<Video> videos, List<Image> images) {
        boolean result = false;
        SQLiteDatabase db = getConnection();
        if (db == null) {
            return result;
        }
        AESTools newTools;
        try {
            db.beginTransaction();
            newTools = new AESTools(Secret.SHAPassword(newPassword +
                    StaticData.finalMixPassword2).getBytes(defaultCharset));
            Iterator<Password> i = data.iterator();
            while (i.hasNext()) {
                Password p = i.next();
                ContentValues cv = new ContentValues();
                String name = Secret.bytesToHexString(newTools.Encrypt(p.name
                        .getBytes(defaultCharset)));
                String username = Secret.bytesToHexString(newTools.Encrypt(p.userName
                        .getBytes(defaultCharset)));
                String password = Secret.bytesToHexString(newTools.Encrypt(p.password
                        .getBytes(defaultCharset)));
                String tip = Secret.bytesToHexString(newTools.Encrypt(p.tip
                        .getBytes(defaultCharset)));
                cv.put("name", name);
                cv.put("username", username);
                cv.put("password", password);
                cv.put("tips", tip);
                db.update("password", cv, "id = ?",
                        new String[]{p.id + ""});
            }
            ContentValues cv = new ContentValues();
            cv.put("value", Secret.SHAPassword(Secret.SHAPassword(newPassword + StaticData.finalMixPasswordString)));
            int count = db.update("property", cv, "key = ?",
                    new String[]{StaticData.DB_LOGIN_PASSWORD});
            if (count == 0) {
                throw new Exception("修改登录密码无效");
            }
            StaticData staticData = StaticData.getInstance();
            AESTools oldTools = new AESTools(Secret.SHAPassword(oldPassword +
                    StaticData.finalMixPassword2).getBytes(defaultCharset));
            for (Image image : images) {
                if (!FileTools.decrypt(new File(StaticData.fromImageIdToSavePath(image.id)), oldPassword)) {
                    throw new Exception("图片文件解密失败，导致修改登录密码失败 :" + image.toString());
                }
                image.hasDecrypted = true;
                byte[] en = null;
                byte[] th = null;
                Cursor cs = db.rawQuery("SELECT th_encrypt, th FROM image WHERE id = ?", new String[]{image.id + ""});
                if (cs.moveToNext()) {
                    en = cs.getBlob(cs.getColumnIndex(Video.DB_TH_ENCRYPT));
                    th = cs.getBlob(cs.getColumnIndex(Video.DB_TH));
                }
                cs.close();
                if (th != null) {
                    byte[] bitmap = fromThEnToByteArray(en, th, oldTools);
                    if (bitmap != null) {
                        en = fromByteArrayToThEn(bitmap, newTools);
                        ContentValues cv2 = new ContentValues();
                        if (en != null) {
                            cv2.put(Video.DB_TH_ENCRYPT, en);
                        }
                        cv2.put(Video.DB_TH, bitmap);
                        int count2 = 0;
                        count2 = db.update("image", cv2, "id = ?", new String[]{image.id + ""});
                        if (count2 < 1) {
                            throw new Exception("修改图片缩略图错误" + image.toString());
                        }
                    }
                }
            }
            for (Video video : videos) {
                if (!FileTools.decrypt(new File(StaticData.fromVideoIdToSavePath(video.id)), oldPassword)) {
                    throw new Exception("视频文件解密失败，导致修改登录密码失败 :" + video.toString());
                }
                video.hasDecrypted = true;
                byte[] en = null;
                byte[] th = null;
                Cursor cs = db.rawQuery("SELECT th_encrypt, th FROM video WHERE id = ?", new String[]{video.id + ""});
                if (cs.moveToNext()) {
                    en = cs.getBlob(cs.getColumnIndex(Video.DB_TH_ENCRYPT));
                    th = cs.getBlob(cs.getColumnIndex(Video.DB_TH));
                }
                cs.close();
                if (th != null) {
                    byte[] bitmap = fromThEnToByteArray(en, th, oldTools);
                    if (bitmap != null) {
                        en = fromByteArrayToThEn(bitmap, newTools);
                        ContentValues cv2 = new ContentValues();
                        if (en != null) {
                            cv2.put(Video.DB_TH_ENCRYPT, en);
                        }
                        cv2.put(Video.DB_TH, bitmap);
                        int count2 = 0;
                        count2 = db.update("video", cv2, "id = ?", new String[]{video.id + ""});
                        if (count2 < 1) {
                            throw new Exception("修改视频缩略图错误" + video.toString());
                        }
                    }
                }
            }
            result = true;
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        return result;
    }

    /**
     * 向表image中插入一条信息
     *
     * @param p
     * @return 如果返回-1，表示插入失败；否则，返回id
     */

    public int insertImage(Image p, String password, String realPath) {
        int result = -1;
        if (p == null || p.name == null || p.path == null || password == null) {
            return result;
        }

        ContentValues cv = new ContentValues();
        cv.put("name", p.name);
        cv.put("path", p.path);
        Bitmap bitmap = ImageTools.getThumbnail(realPath, 0);
        if (bitmap != null) {
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bao);
            byte[] th = bao.toByteArray();
            try {
                bao.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] temp = null;
            try {
                AESTools tools = new AESTools(Secret.SHAPassword(password +
                        StaticData.finalMixPassword2).getBytes(defaultCharset));
                temp = fromByteArrayToThEn(th, tools);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (temp != null) {
                cv.put(Image.DB_TH_ENCRYPT, temp);
            }
            cv.put(Image.DB_TH, th);
        }
        SQLiteDatabase db = getConnection();
        if (db == null) {
            return result;
        }
        long count = db.insert("image", null, cv);
        if (count != -1) {
            Cursor cs = db
                    .rawQuery(
                            "SELECT id FROM image WHERE path = ? ",
                            new String[]{p.path});
            if (cs.moveToNext()) {
                result = cs.getInt(cs.getColumnIndex("id"));
            }
            cs.close();
        }

        return result;
    }

    /**
     * 向表video中插入一条信息
     *
     * @param p
     * @return 如果返回-1，表示插入失败；否则，返回id
     */
    public int insertVideo(Video p, String password, String realPath) {
        int result = -1;
        if (p == null || p.name == null || p.path == null || password == null
                || p.duration == null) {
            return result;
        }

        ContentValues cv = new ContentValues();
        cv.put("name", p.name);
        cv.put("path", p.path);
        cv.put("duration", p.duration);
        Bitmap bitmap = ImageTools.getVideoImage(realPath, 0);
        if (bitmap != null) {
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bao);
            byte[] th = bao.toByteArray();
            try {
                bao.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] temp = null;
            try {
                AESTools tools = new AESTools(Secret.SHAPassword(password +
                        StaticData.finalMixPassword2).getBytes(defaultCharset));
                temp = fromByteArrayToThEn(th, tools);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (temp != null) {
                cv.put(Image.DB_TH_ENCRYPT, temp);
            }
            cv.put(Image.DB_TH, th);
        }
        SQLiteDatabase db = getConnection();
        if (db == null) {
            return result;
        }
        long count = db.insert("video", null, cv);
        if (count != -1) {
            Cursor cs = db
                    .rawQuery(
                            "SELECT id FROM video WHERE path = ? ",
                            new String[]{p.path});
            if (cs.moveToNext()) {
                result = cs.getInt(cs.getColumnIndex("id"));
            }
            cs.close();
        }

        return result;
    }


    /**
     * 向表password中插入一条信息
     *
     * @param p
     * @return 如果返回-1，表示插入失败；否则，返回id
     */

    public int insertPassword(Password p, String realPassword) {
        int result = -1;
        if (p == null || realPassword == null || p.name == null || p.time == null
                || p.userName == null || p.password == null || p.tip == null || p.lastTime == null) {
            return result;
        }
        AESTools t;
        try {
            t = new AESTools(Secret.SHAPassword(realPassword +
                    StaticData.finalMixPassword2).getBytes(defaultCharset));
            ContentValues cv = new ContentValues();
            String name = Secret.bytesToHexString(t.Encrypt(p.name
                    .getBytes(defaultCharset)));
            String username = Secret.bytesToHexString(t.Encrypt(p.userName
                    .getBytes(defaultCharset)));
            String password = Secret.bytesToHexString(t.Encrypt(p.password
                    .getBytes(defaultCharset)));
            String tip = Secret.bytesToHexString(t.Encrypt(p.tip
                    .getBytes(defaultCharset)));
            cv.put("time", p.time);
            cv.put("name", name);
            cv.put("username", username);
            cv.put("password", password);
            cv.put("tips", tip);
            cv.put("lasttime", p.lastTime);
            SQLiteDatabase db = getConnection();
            if (db == null) {
                return result;
            }
            long count = db.insert("password", null, cv);
            if (count != -1) {
                Cursor cs = db
                        .rawQuery(
                                "SELECT id FROM password WHERE lasttime = ? AND time = ? AND name = ? AND username = ? and password = ? AND tips = ? ",
                                new String[]{p.lastTime, p.time, name, username, password, tip});
                if (cs.moveToNext()) {
                    result = cs.getInt(cs.getColumnIndex("id"));
                }
                cs.close();
            }
        } catch (UnsupportedEncodingException e) {
            // 从不发生
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 根据缩略图加密后的两个数组，还原原来的数组
     *
     * @param en    加密部分
     * @param th    未加密部分
     * @param tools 加密器
     * @return 原来的数组，可能为null
     */
    private static byte[] fromThEnToByteArray(byte[] en, byte[] th, AESTools tools) {
        byte[] result = null;
        if (th != null) {
            if (en == null) {
                result = th;
            } else {
                byte[] r = tools.Decrypt(en);
                if (r.length == 20) {
                    System.arraycopy(r, 0, th, 0, 20);
                    result = th;
                }
            }
        }
        return result;
    }

    /**
     * 将原数组加密
     *
     * @param th    原数组，会对原数组的前二十个进行修改
     * @param tools 加密器
     * @return 加密后的部分数组。另一部分修改对应到th
     */
    private static byte[] fromByteArrayToThEn(byte[] th, AESTools tools) {
        if (th.length > 20) {
            byte[] temp = new byte[20];
            System.arraycopy(th, 0, temp, 0, 20);
            byte[] result = tools.Encrypt(temp);
            if (result != null) {
                for (int i = 0; i < 20; i++) {
                    th[i] = 0;
                }
            }
            return result;
        } else {
            return null;
        }
    }


    /**
     * 获取视频或图片缩略图
     *
     * @param id      视频或图片ID
     * @param isVideo 是否是视频
     * @return 缩略图
     */
    public Bitmap getTh(int id, boolean isVideo, String password) {
        Bitmap result = null;
        if (password == null) {
            return null;
        }
        SQLiteDatabase db = this.getConnection();
        byte[] en = null;
        byte[] th = null;
        if (db != null) {
            Cursor cs = db.rawQuery("SELECT th_encrypt, th FROM " + (isVideo ? "video" : "image") + " WHERE id = ?", new String[]{id + ""});
            if (cs.moveToNext()) {
                en = cs.getBlob(cs.getColumnIndex(Video.DB_TH_ENCRYPT));
                th = cs.getBlob(cs.getColumnIndex(Video.DB_TH));
                //Logger.e("VIDEO_TH", id + "  " + (en == null ? null : en.toString()));
            }
            cs.close();
        }
        AESTools tools = null;
        try {
            tools = new AESTools(Secret.SHAPassword(password +
                    StaticData.finalMixPassword2).getBytes(defaultCharset));
            byte[] nowResult = fromThEnToByteArray(en, th, tools);
            if (nowResult != null) {
                result = BitmapFactory.decodeByteArray(nowResult, 0, nowResult.length);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取所有加密的图片信息（不包括缩略图）
     *
     * @return 所有加密的图片
     */
    public List<Image> getAllImages() {
        List<Image> result = new ArrayList<>();
        SQLiteDatabase db = this.getConnection();
        if (db != null) {
            Cursor cs = db.rawQuery("SELECT id, name, path FROM image", new String[]{});
            while (cs.moveToNext()) {
                Image image = new Image();
                image.id = cs.getInt(cs.getColumnIndex("id"));
                image.name = cs.getString(cs.getColumnIndex("name"));
                image.path = cs.getString(cs.getColumnIndex("path"));
                result.add(image);
            }
            cs.close();
        }
        return result;
    }

    /**
     * 获取所有加密的视频信息（不包括缩略图）
     *
     * @return 所有加密的视频
     */
    public List<Video> getAllVideos() {
        List<Video> result = new ArrayList<>();
        SQLiteDatabase db = this.getConnection();
        if (db != null) {
            Cursor cs = db.rawQuery("SELECT id, duration, name, path FROM video", new String[]{});
            while (cs.moveToNext()) {
                Video video = new Video();
                video.id = cs.getInt(cs.getColumnIndex("id"));
                video.duration = cs.getString(cs.getColumnIndex("duration"));
                video.name = cs.getString(cs.getColumnIndex("name"));
                video.path = cs.getString(cs.getColumnIndex("path"));
                result.add(video);
            }
            cs.close();
        }
        return result;
    }

    /**
     * 获取所有的密码信息
     *
     * @return
     */
    public List<Password> getAllPassword(String password) {
        List<Password> result = new ArrayList<Password>();
        if (password == null) {
            return result;
        }
        SQLiteDatabase db = getConnection();
        if (db == null) {
            return result;
        }
        Cursor cs = db.rawQuery("SELECT * FROM password", new String[]{});
        AESTools tools = null;
        try {
            tools = new AESTools(Secret.SHAPassword(password +
                    StaticData.finalMixPassword2).getBytes(defaultCharset));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        while (cs.moveToNext()) {
            Password p = new Password();
            p.id = cs.getInt(cs.getColumnIndex("id"));
            p.time = cs.getString(cs.getColumnIndex("time"));
            p.lastTime = cs.getString(cs.getColumnIndex("lasttime"));
            p.name = cs.getString(cs.getColumnIndex("name"));
            p.userName = cs.getString(cs.getColumnIndex("username"));
            p.password = cs.getString(cs.getColumnIndex("password"));
            p.tip = cs.getString(cs.getColumnIndex("tips"));
            try {
                byte[] temp = tools.Decrypt(Secret
                        .hexStringToBytes(p.name));
                AESTools nowTools = tools;
                if (temp == null) {
                    nowTools = new AESTools(Secret.SHAPassword(null +
                            StaticData.finalMixPassword2).getBytes(defaultCharset));
                }
                p.name = new String(nowTools.Decrypt(Secret
                        .hexStringToBytes(p.name)), defaultCharset);
                p.userName = new String(nowTools.Decrypt(Secret
                        .hexStringToBytes(p.userName)), defaultCharset);
                p.password = new String(nowTools.Decrypt(Secret
                        .hexStringToBytes(p.password)), defaultCharset);
                p.tip = new String(
                        nowTools.Decrypt(Secret.hexStringToBytes(p.tip)),
                        defaultCharset);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            result.add(p);
        }
        cs.close();
        return result;
    }

    /**
     * 将键值对存入表property
     *
     * @param key
     * @param value
     * @return 存入失败或者key为null或者value为null，返回false
     */
    public boolean setProperty(String key, String value) {
        boolean result = false;
        if (key == null || value == null) {
            return result;
        }
        SQLiteDatabase db = getConnection();
        if (db == null) {
            return result;
        }
        boolean has = false;
        Cursor cursor = db.rawQuery("SELECT * FROM property WHERE key = ?",
                new String[]{key});
        if (cursor.moveToNext()) {
            has = true;
        }
        cursor.close();
        if (has) {
            ContentValues cv = new ContentValues();
            cv.put("value", value);
            int count = db.update("property", cv, "key = ?",
                    new String[]{key});
            if (count != 0) {
                result = true;
            }
        } else {
            ContentValues cv = new ContentValues();
            cv.put("key", key);
            cv.put("value", value);
            long count = db.insert("property", null, cv);
            if (count != -1) {
                result = true;
            }
        }
        return result;
    }

    /**
     * 获取表property的一项(key, value)
     *
     * @param name key
     * @return value 如果没有或出错返回null
     */
    public String getProperty(String name) {
        String result = null;
        if (name == null) {
            return null;
        }
        SQLiteDatabase db = getConnection();
        if (db == null) {
            return null;
        }
        Cursor cursor = db.rawQuery("SELECT * FROM property WHERE key = ?",
                new String[]{name});
        if (cursor.moveToNext()) {
            result = cursor.getString(cursor.getColumnIndex("value"));
        }
        cursor.close();
        return result;
    }

    public synchronized boolean initConnection() {
        if (sQLiteDatabase == null || !sQLiteDatabase.isOpen()
                || sQLiteDatabase.isReadOnly()) {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/cn.edu.ustc.software.hanyizhao.encryptiontool/encryption.db");
            if (!file.exists()) {
                if (!file.getParentFile().mkdirs()) {
                    return false;
                }
                //创建.nomedia文件
                File mediaFile = new File(file.getParentFile(), ".nomedia");
                if (!mediaFile.exists()) {
                    try {
                        mediaFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //创建readme.txt
                File readMeFile = new File(file.getParentFile(), "不要删除这个文件夹哦，否则所有加密的数据丢失！.txt");
                if (!readMeFile.exists()) {
                    try {
                        readMeFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //创建数据库文件
                try {
                    if (!file.createNewFile())
                        return false;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            try {
                sQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(file,
                        null);
                if (sQLiteDatabase != null) {
                    if (sQLiteDatabase.getVersion() == 0) {
                        sQLiteDatabase
                                .execSQL("CREATE TABLE property (key NTEXT primary key, value NTEXT)");
                        sQLiteDatabase
                                .execSQL("CREATE TABLE password (id INTEGER PRIMARY KEY AUTOINCREMENT, time NTEXT, lasttime NTEXT, name NTEXT, username NTEXT, password NTEXT, tips NTEXT)");
                        sQLiteDatabase.execSQL("CREATE TABLE video (id INTEGER PRIMARY KEY AUTOINCREMENT, duration TEXT, path NTEXT UNIQUE, name NTEXT, th_encrypt BLOB, th BLOB)");
                        sQLiteDatabase.execSQL("CREATE TABLE image (id INTEGER PRIMARY KEY AUTOINCREMENT, path NTEXT UNIQUE, name NTEXT, th_encrypt BLOB, th BLOB)");
                        sQLiteDatabase.setVersion(1);
                    }
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (sQLiteDatabase != null) {
                    sQLiteDatabase.close();
                }
            }
            return false;
        } else {
            return true;
        }
    }

    private synchronized SQLiteDatabase getConnection() {
        if (sQLiteDatabase == null || !sQLiteDatabase.isOpen()
                || sQLiteDatabase.isReadOnly()) {
            initConnection();
        }
        return sQLiteDatabase;
    }

    /**
     * 手动关闭数据库
     */
    public void close() {
        if (sQLiteDatabase != null && sQLiteDatabase.isOpen()) {
            sQLiteDatabase.close();
        }
    }
}
