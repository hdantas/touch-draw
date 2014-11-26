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
 * Created by hdantas on 8/11/14.
 * Convenience class to store and remove bitmaps in/from public or private directories.
 * It doesn't notify the Media content provider.
 */
public class BitmapUtils extends FileUtils {
    private static final String TAG = BitmapUtils.class.getSimpleName();
    private static final String DIRECTORY_PICTURES_OLD_API = "DCIM";

    public static File saveBitmapToAlbum
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

    private static File saveBitmap
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
    private static File getAlbumPublicExternalStorageDir(Context context) {
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
