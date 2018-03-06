package cn.edu.ustc.software.hanyizhao.encryptiontool.tools;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by HanYizhao on 2015/9/22.
 */
public class Secret {

    public static byte[] SHA(byte[] password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return md.digest(password);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * 计算密码的散列值
     *
     * @param p 密码
     * @return 散列结果。 如果出现异常，返回空字符串
     */
    public static String SHAPassword(String p) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] tempv = md.digest(p.getBytes("UTF8"));
            return bytesToHexString(tempv);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Convert byte[] to hex 小写形式
     * string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
     *
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * Convert hex string to byte[]
     *
     * @param hexString
     * @return
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        // hexString = hexString.toUpperCase(); // 如果是大写形式
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    public static byte charToByte(char c) {
        return (byte) "0123456789abcdef".indexOf(c);
        // return (byte) "0123456789ABCDEF".indexOf(c);
    }

}
