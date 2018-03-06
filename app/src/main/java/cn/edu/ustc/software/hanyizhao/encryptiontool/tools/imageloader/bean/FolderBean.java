package cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by HanYizhao on 2015/12/10.
 * FolderBean
 */
public class FolderBean {
    public String folderName;
    public File folder;
    public boolean briefMode = false;
    public List<MediaPath> files = new ArrayList<>();
}
