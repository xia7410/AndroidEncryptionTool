package cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;

public class MyImageFilter implements FileFilter {

        private static HashSet<String> fileType = new HashSet<String>();
        static {
            fileType.add("jpg");
            fileType.add("jpeg");
            fileType.add("bmp");
            fileType.add("png");
            fileType.add("gif");
            fileType.add("tiff");
            fileType.add("ico");
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