package cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by HanYizhao on 2015/12/10.
 * FolderBean
 */
public class FolderBean {
    /**
     * The shown name of this folder.
     */
    public String folderName;
    /**
     * Location of this folder.
     */
    public File folder;
    /**
     * If ture, {@link #files} only stores names of files.
     * If false, {@link #files} stores absolute names of files.
     */
    public boolean briefMode = false;
    /**
     * Files in this folder. Directory is not included.
     */
    public List<MediaPath> files = new ArrayList<>();

    /**
     * There is only one folder is selected at the same time.
     */
    public boolean isSelected = false;

    public boolean isVideoFolder = false;

}
