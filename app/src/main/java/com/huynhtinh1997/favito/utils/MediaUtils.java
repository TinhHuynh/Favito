package com.huynhtinh1997.favito.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import com.huynhtinh1997.favito.model.FileHolder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by huynhtinh1997 on 26/06/2017.
 */

public class MediaUtils {
    public static final int PHOTO = 0;
    public static final int VIDEO = 1;
    private static final String TAG = "MediaUtils";
    // photo format //
    private static final String JPEG = ".jpg";
    private static final String PNG = ".png";
    // video format //
    private static final String MP4 = ".mp4";
    private static final String FLV = ".flv";
    private static final String PREFIX_PHOTO_FILE_NAME = "JPEG_";
    private static final String PREFIX_VIDEO_FILE_NAME = "MP4_";

    public static List<FileHolder> getFileHolders() {
        File externalStorageDir = Environment.getExternalStorageDirectory();
        List<FileHolder> photoPaths = new ArrayList<>();
        findFiles(externalStorageDir, photoPaths);
        return photoPaths;
    }

    private static void findFiles(File file, List<FileHolder> photoPaths) {
        if (file.isDirectory()) {
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

    private static void initImageLoader(Context context) {
        ImageLoaderConfiguration configuration = ImageLoaderConfiguration.createDefault(context);
        ImageLoader.getInstance().init(configuration);
    }

    public static void loadThumbnail(Context context, File file, ImageView thumbnailImageVIew) {
        initImageLoader(context);
        Uri uri = Uri.fromFile(file);
        Log.d(TAG, uri.toString());
        ImageLoader.getInstance().displayImage(uri.toString(), thumbnailImageVIew);
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

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
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
