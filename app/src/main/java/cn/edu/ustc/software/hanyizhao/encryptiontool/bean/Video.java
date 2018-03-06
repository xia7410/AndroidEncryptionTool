package cn.edu.ustc.software.hanyizhao.encryptiontool.bean;

/**
 * Created by HanYizhao on 2015/12/24.
 * 实体类
 */
public class Video {
    public int id;
    public String path;
    public String duration;
    public String name;
    public boolean hasDecrypted = false;

    public static final String DB_ID = "id";
    public static final String DB_PATH = "path";
    public static final String DB_DURATION = "duration";
    public static final String DB_NAME = "name";
    public static final String DB_TH_ENCRYPT = "th_encrypt";
    public static final String DB_TH = "th";

}
