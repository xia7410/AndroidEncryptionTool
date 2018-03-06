package cn.edu.ustc.software.hanyizhao.encryptiontool.bean;

import java.io.Serializable;

/**
 * Created by HanYizhao on 2015/9/22.
 */
public class Password implements Serializable {
    public int id = 0;
    public String time;
    public String lastTime;
    public String name;
    public String userName;
    public String password;
    public String tip;

    private String stringTime;
    private String stringLastTime;

    /*
    获取最后修改时间的字符串格式
     */
    public String getStringLastTime() {
        if (stringLastTime == null) {
            if (lastTime == null) {
                return "";
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(lastTime.substring(0, 4));
                sb.append("-");
                sb.append(lastTime.substring(4, 6));
                sb.append("-");
                sb.append(lastTime.substring(6, 8));
                sb.append(" ");
                sb.append(lastTime.substring(8, 10));
                sb.append(":");
                sb.append(lastTime.substring(10, 12));
                sb.append(":");
                sb.append(lastTime.substring(12, 14));
                stringLastTime = sb.toString();
                return stringLastTime;
            }
        } else {
            return stringLastTime;
        }
    }

    /*
    获取时间的字符串格式
     */
    public String getStringTime() {
        if (stringTime == null) {
            if (time == null) {
                return "";
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(time.substring(0, 4));
                sb.append("-");
                sb.append(time.substring(4, 6));
                sb.append("-");
                sb.append(time.substring(6, 8));
                sb.append(" ");
                sb.append(time.substring(8, 10));
                sb.append(":");
                sb.append(time.substring(10, 12));
                sb.append(":");
                sb.append(time.substring(12, 14));
                stringTime = sb.toString();
                return stringTime;
            }
        } else {
            return stringTime;
        }
    }

    public boolean equals(Object o) {
        if (o != null && o instanceof Password) {
            Password d = (Password) o;
            return d.name.equals(name) && d.userName.equals(userName) && d.password.equals(password) && d.tip.equals(tip);
        }
        return false;
    }


}
