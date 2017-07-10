package com.huynhtinh1997.favito.model;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.Serializable;

/**
 * Created by huynhtinh1997 on 01/07/2017.
 */

public class FileHolder implements Comparable<FileHolder>, Serializable {
    private File mMediaFile;

    public File getMediaFile() {
        return mMediaFile;
    }

    public void setMediaFile(File mediaFile) {
        mMediaFile = mediaFile;
    }

    public String getFilePath() {
        return mMediaFile.getAbsolutePath();
    }

    public String getFileName() {
        return mMediaFile.getName();
    }

    public long getLastModifiedDate() {
        return mMediaFile.lastModified();
    }

    @Override
    public int compareTo(@NonNull FileHolder anotherFileHolder) {
        if (this.getLastModifiedDate() > anotherFileHolder.getLastModifiedDate()) {
            return -1;
        } else if (this.getLastModifiedDate() < anotherFileHolder.getLastModifiedDate()) {
            return 1;
        } else {
            return 0;
        }
    }
}
