package cn.edu.ustc.software.hanyizhao.encryptiontool.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.AESTools;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.DBTools;
import cn.edu.ustc.software.hanyizhao.encryptiontool.tools.FileTools;
import cn.edu.ustc.software.hanyizhao.encryptiontool.bean.Image;
import cn.edu.ustc.software.hanyizhao.encryptiontool.bean.Password;
import cn.edu.ustc.software.hanyizhao.encryptiontool.bean.Video;

/**
 * Created by HanYizhao on 2015/9/22.
 */
public class StaticData {

    /**
     * 不能修改！作用：在对密码求解SHA值的时候，增加随机性，比如123的SHA大家都知道，但是123加上这个字符串之后
     * 的SHA值很特殊，所以，增加了程序结果的随机性。如果修改，两个版本的程序，无法通用
     */
    public static final String finalMixPasswordString = "04620035489697555705006462563460504275596082135429";

    /**
     * 不能修改！{@link #finalMixPasswordString}，第一个用于登录散列，第二个用于存储加密散列
     */
    public static final String finalMixPassword2 = "JAPXO5XCuseu2aYvK5nWeA6vzZlrGP";

    public static final String DB_LOGIN_PASSWORD = "login_password";
    public static final String DB_PASSWORD_SEARCH_OPTIONS = "password_search_options";
    public static final String DB_PASSWORD_SORT_OPTIONS = "password_sort_options";
    public static final String DB_PASSWORD_SORT_OPTIONS_UP_OR_DOWN = "password_sort_options_up_or_down";

    /**
     */
    public List<Password> data = null;


    private List<Image> imagesData = new ArrayList<>();

    private List<Video> videosData = new ArrayList<>();

    private static String parentPath = Environment.getExternalStorageDirectory().getAbsolutePath();

    private boolean hasReset = false;

    /*
    密码搜索选项 1-6
     */
    public int passwordSearchOptions = 1;

    /*
    密码排序选项 1-6
     */
    public int passwordSortOptions = 1;
    /**
     * 排序箭头上下
     */
    public boolean passwordSortOptionsUp = true;

    private Toast toast = null;
    private String realPassword = null;

    public static SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    public String getRealPassword() {
        return realPassword;
    }

    public void setRealPassword(String realPassword) {
        this.realPassword = realPassword;
    }

    public List<Video> getAllVideoData() {
        return videosData;
    }

    /**
     * 生成时间yyyyMMddHHmmss形式
     *
     * @return
     */
    public static String createTime() {
        return format.format(new Date());
    }


    /**
     * 判断内存和数据库中是否有该密码，通过time判断
     *
     * @param p
     * @return 如果p为空，返回0, 一模一样(time和lasttime相同)，没有修改，返回0 存在但是新的返回 1 存在但是旧的 返回 2 不存在 返回3 重复（time不同，内容相同） 4
     */
    public int hasPasswordByTime(Password p) {
        boolean result = false;
        if (p == null) {
            return 0;
        }
        for (Password i : data) {
            if (i.time.equals(p.time)) {
                int a = i.lastTime.compareTo(p.lastTime);
                if (a == 0) {
                    return 0;
                } else if (a > 0) {
                    return 2;
                } else if (a < 0) {
                    return 1;
                }
            } else {
                if (i.equals(p)) {
                    return 4;
                }
            }
        }
        return 3;
    }


    /**
     * 判断内存和数据库中是否有该密码
     *
     * @param p
     * @return 如果p为空，返回true
     */
    public boolean hasPassword(Password p) {
        boolean result = false;
        if (p == null) {
            return true;
        }
        Iterator<Password> i = data.iterator();
        while (i.hasNext()) {
            Password a = i.next();
            if (a.equals(p)) {
                result = true;
                break;
            }
        }
        return result;
    }


    private int getPasswordPositionByTime(String time) {
        int result = -1;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).time.equals(time)) {
                result = i;
                break;
            }
        }
        return result;
    }

    /**
     * 获取项目对应的位置
     *
     * @param p
     * @return
     */
    private int getPasswordPositionById(Password p) {
        int result = -1;
        for (int i = 0; i < data.size(); i++) {
            if (p.id == data.get(i).id) {
                result = i;
                break;
            }
        }
        return result;
    }

    /**
     * 删除项目，同步数据库和内存
     *
     * @param c
     * @param p
     * @return
     */
    public boolean deletePassword(Context c, List<Password> p) {
        boolean result = true;
        DBTools t = DBTools.getInstance();
        for (int i = 0; i < p.size(); i++) {
            if (t.deletePassword(p.get(i).id)) {
                int id = getPasswordPositionById(p.get(i));
                if (id != -1)
                    data.remove(id);
            } else {
                result = false;
            }
        }
        t.close();
        return result;
    }

    /**
     * 删除项目，同步数据库和内存
     *
     * @param c
     * @param p
     * @return
     */
    public boolean deletePassword(Context c, Password p) {
        boolean result = false;
        DBTools t = DBTools.getInstance();
        if (t.deletePassword(p.id)) {
            result = true;
            int id = getPasswordPositionById(p);
            if (id != -1)
                data.remove(id);
        }
        t.close();
        return result;
    }

    /**
     * 修改项目，同步数据库和内存 相同Time的项目
     *
     * @param c
     * @param p
     * @return
     */
    public boolean setPasswordByTime(Context c, Password p) {
        boolean result = false;
        DBTools t = DBTools.getInstance();
        if (t.setPassword(p, realPassword, true)) {
            result = true;
            int position = getPasswordPositionByTime(p.time);
            if (position != -1) {
                p.id = data.get(position).id;
                data.set(position, p);
            }
        }
        t.close();
        return result;
    }

    /**
     * 修改项目，同步数据库和内存
     *
     * @param c
     * @param p
     * @return
     */
    public boolean setPasswordById(Context c, Password p) {
        boolean result = false;
        DBTools t = DBTools.getInstance();
        p.lastTime = createTime();
        if (t.setPassword(p, realPassword, false)) {
            result = true;
            int position = getPasswordPositionById(p);
            if (position != -1) {
                data.set(position, p);
            }
        }
        t.close();
        return result;
    }

    public boolean isHasReset() {
        return hasReset;
    }

    public void resetData() {
        if (!hasReset) {
            hasReset = true;
            DBTools db = DBTools.getInstance();
            // 数据
            data = db.getAllPassword(realPassword);
            String temp = db.getProperty(DB_PASSWORD_SEARCH_OPTIONS);
            if (temp != null) {
                passwordSearchOptions = Integer.parseInt(temp);
            } else {
                passwordSearchOptions = 1;
                db.setProperty(DB_PASSWORD_SEARCH_OPTIONS, 1 + "");
            }
            temp = db.getProperty(DB_PASSWORD_SORT_OPTIONS);
            if (temp != null) {
                passwordSortOptions = Integer.parseInt(temp);
            } else {
                passwordSortOptions = 1;
                db.setProperty(DB_PASSWORD_SORT_OPTIONS, 1 + "");
            }
            temp = db.getProperty(DB_PASSWORD_SORT_OPTIONS_UP_OR_DOWN);
            if (temp != null) {
                passwordSortOptionsUp = Boolean.parseBoolean(temp);
            } else {
                passwordSortOptionsUp = true;
                db.setProperty(DB_PASSWORD_SORT_OPTIONS_UP_OR_DOWN, true + "");
            }
            imagesData = db.getAllImages();
            //数据库中存储为 /DCIM/Camera/XXX.png 修改为 /sdcard/sdcard/DCIM...
            for (Image i : imagesData) {
                i.path = parentPath + i.path;
            }
            //判断文件是否存在 并尝试加密（防止上次没有正确退出）
            List<Integer> shouldDelete = new ArrayList<>();
            for (Image i : imagesData) {
                if (!FileTools.encrypt(new File(fromImageIdToSavePath(i.id)), getRealPassword())) {
                    db.deleteImage(i.id);
                    shouldDelete.add(i.id);
                }
            }
            //将不存在的文件从data中删除
            for (Integer i : shouldDelete) {
                int nowId = getImagePositionById(i);
                imagesData.remove(nowId);
            }
            shouldDelete.clear();
            videosData = db.getAllVideos();
            //数据库中存储为 /DCIM/Camera/XXX.png 修改为 /sdcard/sdcard/DCIM...
            for (Video i : videosData) {
                i.path = parentPath + i.path;
            }
            //判断文件是否存在
            for (Video i : videosData) {
                if (!FileTools.encrypt(new File(fromVideoIdToSavePath(i.id)), getRealPassword())) {
                    db.deleteVideo(i.id);
                    shouldDelete.add(i.id);
                }
            }
            //将不存在的文件从data中删除
            for (Integer i : shouldDelete) {
                int nowId = getVideoPositionById(i);
                videosData.remove(nowId);
            }
            db.close();
        }
    }

    private static StaticData staticData = null;

    private StaticData() {
    }

    public static StaticData getInstance() {
        if (staticData == null) {
            staticData = new StaticData();
        }
        return staticData;
    }

    /**
     * 根据video id获取视频位置
     *
     * @param id 视频ID
     * @return 视频位置
     */
    public static String fromVideoIdToSavePath(int id) {
        return parentPath + "/cn.edu.ustc.software.hanyizhao.encryptiontool/Video/" + id;
    }

    /**
     * 根据image id获取图片位置
     *
     * @param id 图片ID
     * @return 图片位置
     */
    public static String fromImageIdToSavePath(int id) {

        return parentPath + "/cn.edu.ustc.software.hanyizhao.encryptiontool/Image/" + id;
    }


    /**
     * 修改密码搜索选项
     *
     * @param value
     * @return
     */
    public boolean setPasswordSearchOptions(Context c, int value) {
        boolean result = false;
        DBTools dbTools = DBTools.getInstance();
        if (dbTools.setProperty(DB_PASSWORD_SEARCH_OPTIONS, value + "")) {
            result = true;
            this.passwordSearchOptions = value;
        }
        dbTools.close();
        return result;
    }

    /**
     * 修改密码排序选项
     *
     * @param value
     * @return
     */
    public boolean setPasswordSortOptions(Context c, int value, boolean isUp) {
        boolean result = false;
        DBTools dbTools = DBTools.getInstance();
        if (dbTools.setProperty(DB_PASSWORD_SORT_OPTIONS, value + "")) {
            result = true;
            this.passwordSortOptions = value;
        }
        if (dbTools.setProperty(DB_PASSWORD_SORT_OPTIONS_UP_OR_DOWN, isUp + "")) {
            result = true;
            this.passwordSortOptionsUp = isUp;
        }

        dbTools.close();
        return result;
    }

    /**
     * 添加到数据库和内存 在调用前，请手动确认不重复（通过调用{@link #hasPassword}）
     *
     * @param c
     * @param p
     * @return
     */
    public boolean addPassword(Context c, Password p) {
        boolean result = false;
        if (p == null) {
            return false;
        }
        DBTools t = DBTools.getInstance();
        int id = t.insertPassword(p, realPassword);
        if (id != -1) {
            p.id = id;
            data.add(p);
            result = true;
        }
        return result;
    }

    public void showMessage(String message, Context c, int duration) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(c, message, duration);
        toast.show();
    }

    public void showMessage(String message, Context c) {
        showMessage(message, c, Toast.LENGTH_SHORT);
    }

    public void stopMessage() {
        if (toast != null) {
            toast.cancel();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    public List<Image> getAllImageData() {
        return imagesData;
    }


    /**
     * 判断是否存在这个路径。防止重复添加图片
     *
     * @param path 完整路径
     * @return
     */
    private boolean hasImagePath(String path) {
        boolean flag = false;
        for (Image i : imagesData) {
            if (i.path.equals(path)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     * 删除项目，同步数据库和内存
     *
     * @param id
     * @return
     */
    public boolean deleteImage(int id) {
        boolean result = false;
        DBTools t = DBTools.getInstance();
        if (t.deleteImage(id)) {
            result = true;
            int id2 = getImagePositionById(id);
            if (id2 != -1)
                imagesData.remove(id2);
        }
        t.close();
        return result;
    }

    /**
     * 添加图片
     *
     * @param path 图片完整路径
     * @return
     */
    public int addImage(String path) {
        int result;
        if (hasImagePath(path)) {
            result = -1;
        } else {
            Image v = new Image();
            v.path = path.substring(parentPath.length());
            v.name = new File(path).getName();
            if (v.name.contains(".")) {
                v.name = v.name.substring(0, v.name.lastIndexOf("."));
            }
            DBTools dbTools = DBTools.getInstance();
            int id = dbTools.insertImage(v, getRealPassword(), path);
            dbTools.close();
            if (id != -1) {
                v.id = id;
                v.path = path;
                imagesData.add(v);
                result = v.id;
            } else {
                result = -1;
            }
        }
        return result;

    }

    /**
     * 获取项目对应的位置
     *
     * @param id
     * @return
     */
    private int getImagePositionById(int id) {
        int result = -1;
        for (int i = 0; i < imagesData.size(); i++) {
            if (id == imagesData.get(i).id) {
                result = i;
                break;
            }
        }
        return result;
    }

    /**
     * 判断是否存在这个路径。防止重复添加视频
     *
     * @param path 完整路径
     * @return
     */
    private boolean hasVideoPath(String path) {
        boolean flag = false;
        for (Video i : videosData) {
            if (i.path.equals(path)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     * 删除项目，同步数据库和内存
     *
     * @param id
     * @return
     */
    public boolean deleteVideo(int id) {
        boolean result = false;
        DBTools t = DBTools.getInstance();
        if (t.deleteVideo(id)) {
            result = true;
            int id2 = getVideoPositionById(id);
            if (id2 != -1)
                videosData.remove(id2);
        }
        t.close();
        return result;
    }

    /**
     * 添加视频
     *
     * @param path     视频完整路径
     * @param duration 视频持续时间
     * @return 视频ID,-1 表示失败
     */
    public int addVideo(String path, String duration) {
        int result;
        if (hasVideoPath(path)) {
            result = -1;
        } else {
            Video v = new Video();
            v.path = path.substring(parentPath.length());
            v.duration = duration;
            v.name = new File(path).getName();
            if (v.name.contains(".")) {
                v.name = v.name.substring(0, v.name.lastIndexOf("."));
            }
            DBTools dbTools = DBTools.getInstance();
            int id = dbTools.insertVideo(v, getRealPassword(), path);
            dbTools.close();
            if (id != -1) {
                v.id = id;
                v.path = path;
                videosData.add(v);
                result = v.id;
            } else {
                result = -1;
            }
        }
        return result;

    }

    /**
     * 获取项目对应的位置
     *
     * @param id 视频ID
     * @return 在列表中的位置
     */
    private int getVideoPositionById(int id) {
        int result = -1;
        for (int i = 0; i < videosData.size(); i++) {
            if (id == videosData.get(i).id) {
                result = i;
                break;
            }
        }
        return result;
    }

    public synchronized Bitmap getTh(int id, boolean isVideo) {
        DBTools dbTools = DBTools.getInstance();
        Bitmap b = dbTools.getTh(id, isVideo, realPassword);
        dbTools.close();
        return b;
    }

    public synchronized boolean modifyLoginPassword(String newPassword) {
        DBTools db = DBTools.getInstance();
        if (db.modifyLoginPassword(realPassword, newPassword, data, videosData, imagesData)) {
            realPassword = newPassword;
            return true;
        } else {
            return false;
        }
    }
}
