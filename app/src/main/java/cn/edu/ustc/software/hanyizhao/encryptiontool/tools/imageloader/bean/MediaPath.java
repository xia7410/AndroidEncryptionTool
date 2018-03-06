package cn.edu.ustc.software.hanyizhao.encryptiontool.tools.imageloader.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Created by HanYizhao on 2015/12/16.
 */
public class MediaPath implements Parcelable {
    public TaskType type;
    public String path;
    public String duration;


    @Override
    public boolean equals(Object o) {
        if (o instanceof MediaPath) {
            MediaPath temp = (MediaPath) o;
            return path == null ? temp.path == null : path.equals(temp.path);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    public MediaPath(TaskType type, @NonNull String path) {
        this(type, path, null);
    }

    public MediaPath(TaskType type, @NonNull String path, String duration) {
        this.type = type;
        this.path = path;
        this.duration = duration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private MediaPath(Parcel source) {
        this.type = TaskType.values()[source.readInt()];
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
        dest.writeInt(type.ordinal());
        dest.writeString(path);
        dest.writeString(duration);
    }
}
