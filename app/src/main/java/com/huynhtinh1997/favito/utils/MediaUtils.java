package com.huynhtinh1997.favito.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import com.huynhtinh1997.favito.model.FileHolder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by huynhtinh1997 on 26/06/2017.
 */

public class MediaUtils {
    public static final int PHOTO = 0;
    public static final int VIDEO = 1;
    // photo format //
    private static final String JPEG = ".jpg";
    private static final String PNG = ".png";
    // video format //
    private static final String MP4 = ".mp4";
    private static final String FLV = ".flv";
    private static final String PREFIX_PHOTO_FILE_NAME = "JPEG_";
    private static final String PREFIX_VIDEO_FILE_NAME = "MP4_";
    // default directory;
    private static final String DIRECTORY_EXTERNAL_STORAGE =
            Environment.getExternalStorageDirectory().getAbsolutePath();

    private static final String DIRECTORY_DCIM =
            DIRECTORY_EXTERNAL_STORAGE + "/" + Environment.DIRECTORY_DCIM;

    private static final String DIRECTORY_DOWNLOADS =
            DIRECTORY_EXTERNAL_STORAGE + "/" + Environment.DIRECTORY_DOWNLOADS;

    private static final String DIRECTORY_PICTURES =
            DIRECTORY_EXTERNAL_STORAGE + "/" + Environment.DIRECTORY_PICTURES;

    private static final String TAG = "MediaUtils";

    public static List<FileHolder> getFileHolders() {
        File externalStorageDir = new File(DIRECTORY_EXTERNAL_STORAGE);
        List<FileHolder> fileHolders = new ArrayList<>();
        findFiles(externalStorageDir, fileHolders);
        Collections.sort(fileHolders);
        return fileHolders;
    }

    private static void findFiles(File file, List<FileHolder> photoPaths) {
        if (file.isDirectory() && checkSupportDirectory(file)) {

            for (File subFile : file.listFiles()) {
                findFiles(subFile, photoPaths);
            }
        } else if (file.isFile()
                && file.length() > 0
                && (checkSupportPhotoFile(file) || checkSupportVideoFile(file))) {

            FileHolder fileHolder = new FileHolder();
            fileHolder.setMediaFile(file);
            photoPaths.add(fileHolder);

        }
    }

    private static boolean checkSupportDirectory(File file) {
        String path = file.getAbsolutePath();
        String parentPath = file.getParentFile().getAbsolutePath();
        return path.equals(DIRECTORY_EXTERNAL_STORAGE) ||
                path.equals(DIRECTORY_DCIM) ||
                path.equals(DIRECTORY_DOWNLOADS) ||
                path.equals(DIRECTORY_PICTURES) ||
                // if directory is sub-directory of support ones
                parentPath.equals(DIRECTORY_DCIM) ||
                parentPath.equals(DIRECTORY_DOWNLOADS) ||
                parentPath.equals(DIRECTORY_PICTURES);
    }

    public static boolean checkSupportPhotoFile(File file) {
        String path = file.getAbsolutePath();
        return path.endsWith(JPEG)
                || path.endsWith(PNG);
    }

    public static boolean checkSupportVideoFile(File file) {
        String path = file.getPath();
        return path.endsWith(MP4)
                || path.endsWith(FLV);
    }

    public static File createFile(int mediaKind) throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = (mediaKind == PHOTO ?
                PREFIX_PHOTO_FILE_NAME : PREFIX_VIDEO_FILE_NAME) + timeStamp + "_";

        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File favitoDir = new File(storageDir.getAbsolutePath() + "/Favito");
        if (!favitoDir.isDirectory()) {
            favitoDir.mkdir();
        }
        return File.createTempFile(
                fileName,
                mediaKind == PHOTO ? JPEG : MP4,
                favitoDir
        );
    }

    public static void addFileToGallery(Context context, File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

}
