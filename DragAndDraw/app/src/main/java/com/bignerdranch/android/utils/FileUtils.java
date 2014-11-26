package com.bignerdranch.android.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by hdantas on 8/11/14.
 * Convenience class to store and remove files in/from public or private directories.
 */
class FileUtils {
    private static final String TAG = FileUtils.class.getSimpleName();

    public static File saveFileToPrivateInternalStorage
            (Context context, String filename, byte[] fileContent) {
        return saveFile(getPrivateInternalStorageDir(context), filename, fileContent);
    }

    public static File saveFileToCache(Context context, String filename, byte[] fileContent) {
        return saveFile(context.getCacheDir(), filename, fileContent);
    }

    private static File saveFile(File dir, String filename, byte[] fileContent) {
        try {
            File file = new File(dir, filename);
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(fileContent);
            outputStream.close();
            return file;
        } catch (IOException e) {
            Log.e(TAG, "Error writing file " + dir.getPath(), e);
            return null;
        }
    }

    /* Checks if external storage is available for read and write */
    static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    /* Save files on the private internal storage (removed when app is uninstalled) */
    static File getPrivateInternalStorageDir(Context context) {
        return context.getFilesDir();
    }


    static File makeDir(File dir) {
        if (dir.mkdirs() || dir.isDirectory()) {
            return dir;
        } else {
            Log.e(TAG, "Failed to create directory " + dir.getPath());
            return null;
        }
    }

    /* Save private files to the root of the private dir in external storage
    (removed when app is uninstalled) */
    protected static File getRootExternalStorageDir(Context context) {
        File dir;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            dir = context.getExternalFilesDir(null);
        } else {
            dir = new File(context.getFilesDir(), "/");
            makeDir(dir);
        }
        return dir;
    }

    public static boolean deleteFile(File file) {
        return file.delete();
    }

    /* Delete file from internal storage */
    public static boolean deleteInternalStorageFile(Context context, String filename) {
        return context.deleteFile(filename);
    }


}
