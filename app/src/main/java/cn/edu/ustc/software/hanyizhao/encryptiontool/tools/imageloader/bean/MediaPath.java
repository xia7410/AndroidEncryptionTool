package cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Created by HanYizhao on 2015/12/16.
 */
public class MediaPath implements Parcelable, Comparable<MediaPath> {
    public boolean isVideo;
    public String path;
    public String duration;
    /**
     * milliseconds
     */
    public long modify;


    @Override
    public boolean equals(Object o) {
        if (o instanceof MediaPath) {
            MediaPath temp = (MediaPath) o;
            return path == null ? temp.path == null : path.toLowerCase().equals(temp.path.toLowerCase());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return path.toLowerCase().hashCode();
    }

    public MediaPath(boolean isVideo, @NonNull String path, long modify) {
        this(isVideo, path, modify, null);
    }

    public MediaPath(boolean isVideo, @NonNull String path, long modify, String duration) {
        this.isVideo = isVideo;
        this.path = path;
        this.duration = duration;
        this.modify = modify;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private MediaPath(Parcel source) {
        this.isVideo = source.readInt() == 1;
        this.path = source.readString();
        this.duration = source.readString();
    }

    public static final Parcelable.Creator<MediaPath> CREATOR = new Parcelable.Creator<MediaPath>() {

        @Override
        public MediaPath createFromParcel(Parcel source) {
            MediaPath mediaPath = new MediaPath(source);
            return mediaPath;
        }

        @Override
        public MediaPath[] newArray(int size) {
            return new MediaPath[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(isVideo ? 1 : 0);
        dest.writeString(path);
        dest.writeString(duration);
    }

    @Override
    public String toString() {
        return "[" + "isVideo=" + isVideo +
                ", modify=" + modify +
                ", path=" + path +
                ", duration=" + duration + "]";
    }

    @Override
    public int compareTo(@NonNull MediaPath o) {
        long x = o.modify;
        long y = this.modify;
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }
}
