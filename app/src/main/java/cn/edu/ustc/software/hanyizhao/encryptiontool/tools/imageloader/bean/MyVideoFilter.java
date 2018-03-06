package cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;

public class MyVideoFilter implements FileFilter {

        private static HashSet<String> fileType = new HashSet<String>();
        static {
            fileType.add("mov");
            fileType.add("rmvb");
            fileType.add("mkv");
            fileType.add("rm");
            fileType.add("flv");
            fileType.add("mp4");
            fileType.add("avi");
            fileType.add("wmv");
            fileType.add("wmp");
            fileType.add("wm");
            fileType.add("asf");
            fileType.add("mpg");
            fileType.add("mpeg");
            fileType.add("mpe");
            fileType.add("m1v");
            fileType.add("m2v");
            fileType.add("mpv2");
            fileType.add("mp2v");
            fileType.add("ts");
            fileType.add("tp");
            fileType.add("tpr");
            fileType.add("trp");
            fileType.add("vob");
            fileType.add("ifo");
            fileType.add("ogm");
            fileType.add("ogv");
            fileType.add("m4v");
            fileType.add("m4p");
            fileType.add("m4b");
            fileType.add("3gp");
            fileType.add("3gpp");
            fileType.add("3g2");
            fileType.add("3gp2");
            fileType.add("ram");
            fileType.add("rpm");
            fileType.add("swf");
            fileType.add("qt");
            fileType.add("nsv");
            fileType.add("dpg");
            fileType.add("m2ts");
            fileType.add("m2t");
            fileType.add("mts");
            fileType.add("dvr-ms");
            fileType.add("k3g");
            fileType.add("skm");
            fileType.add("evo");
            fileType.add("nsr");
            fileType.add("amv");
            fileType.add("divx");
            fileType.add("webm");
        }

        @Override
        public boolean accept(File pathname) {
            if (pathname == null || !pathname.exists() || pathname.isHidden()
                    || pathname.isDirectory() || pathname.length() < 5 * 1024) {
                return false;
            }
            String b = pathname.getName();
            int a = b.lastIndexOf('.');
            if (a != -1) {
                b = b.substring(a + 1).toLowerCase();
                if (fileType.contains(b)) {
                    return true;
                }
            }
            return false;
        }
    }