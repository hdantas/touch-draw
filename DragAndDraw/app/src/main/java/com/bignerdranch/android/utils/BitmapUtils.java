package com.bignerdranch.android.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.bignerdranch.android.draganddraw.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by nuno on 8/11/14.
 */
public class BitmapUtils extends FileUtils {
    private static final String TAG = BitmapUtils.class.getSimpleName();
    private static final String DIRECTORY_PICTURES_OLD_API = "DCIM";

    // It will save to external storage if present, else it uses internal storage
    public static File saveBitmapToAlbum
    (Context context, String filename, Bitmap bitmap, Bitmap.CompressFormat format) {
        if (isExternalStorageWritable()) {
            return saveBitmapToAlbumPublicExternalStorage(context, filename, bitmap, format);
        } else {
            return null;
        }
    }

    public static File saveBitmapToAlbumPublicExternalStorage
            (Context context, String filename, Bitmap bitmap, Bitmap.CompressFormat format) {
        if (isExternalStorageWritable()) {
            return saveBitmap(getAlbumPublicExternalStorageDir(context), filename, bitmap, format);
        } else {
            return null;
        }
    }

    public static File saveBitmapToPrivateInternalStorage
            (Context context, String filename, Bitmap bitmap, Bitmap.CompressFormat format) {
        return saveBitmap(getPrivateInternalStorageDir(context), filename, bitmap, format);
    }

    protected static File saveBitmap
            (File dir, String filename, Bitmap bitmap, Bitmap.CompressFormat format) {
        try {
            File file = new File(dir, filename);
            FileOutputStream os = new FileOutputStream(file);
            bitmap.compress(format, 100, os);
            return file;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File " + dir.getPath() + "/" + filename + " not found.", e);
            return null;
        }
    }

    /* Save public files on the external storage (persistent even after app is uninstalled) */
    public static File getAlbumPublicExternalStorageDir(Context context) {
        String albumName = context.getResources().getString(R.string.app_name);
        File albumPublicStorage;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            albumPublicStorage = Environment.getExternalStoragePublicDirectory
                    (Environment.DIRECTORY_PICTURES + "/" + albumName);
        } else {
            albumPublicStorage = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/" + DIRECTORY_PICTURES_OLD_API + "/" + albumName);
        }

        return makeDir(albumPublicStorage);
    }


}
