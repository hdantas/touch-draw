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
    protected static final String TAG = BitmapUtils.class.getSimpleName();

    private static final String DIRECTORY_PICTURES_OLD_API = "DCIM";

    public static boolean saveBitmapToAlbumPublicExternalStorage
            (Context context, String filename, Bitmap bitmap, Bitmap.CompressFormat format) {

        return isExternalStorageWritable() &&
                saveBitmap(getAlbumPublicExternalStorageDir(context), filename, bitmap, format);
    }

    public static boolean saveBitmapToPrivateInternalStorage
            (Context context, String filename, Bitmap bitmap, Bitmap.CompressFormat format) {
        return saveBitmap(getPrivateInternalStorageDir(context), filename,  bitmap, format);
    }

    protected static boolean saveBitmap
            (File dir, String filename, Bitmap bitmap, Bitmap.CompressFormat format) {
        boolean success;
        try {
            File file = new File(dir, filename);
            FileOutputStream os = new FileOutputStream(file);
            bitmap.compress(format, 100, os);
            success = true;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File " + dir.getPath() + "/" + filename + " not found.", e);
            success = false;
        }
        return success;
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

        if(!makeDir(albumPublicStorage)) {
            Log.e(TAG, "Failed to create Public Album " + albumPublicStorage.getPath());
        }

        return albumPublicStorage;
    }


}
