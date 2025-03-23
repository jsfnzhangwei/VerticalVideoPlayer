package com.example.verticalvideoplayer.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    
    // 检查存储权限
    public static boolean checkStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11及以上使用新的存储权限API
            return Environment.isExternalStorageManager();
        } else {
            // Android 10及以下使用传统权限
            int readPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
            int writePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return readPermission == PackageManager.PERMISSION_GRANTED && 
                   writePermission == PackageManager.PERMISSION_GRANTED;
        }
    }
    
    // 请求存储权限
    public static void requestStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11及以上使用新的存储权限API
            try {
                // 这里需要使用Intent跳转到系统设置页面，在实际应用中实现
                // Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                // Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                // intent.setData(uri);
                // activity.startActivityForResult(intent, PERMISSION_REQUEST_CODE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Android 10及以下使用传统权限
            ActivityCompat.requestPermissions(activity, 
                new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 
                PERMISSION_REQUEST_CODE);
        }
    }
    
    // 获取常用视频目录
    public static List<File> getCommonVideoDirectories() {
        List<File> directories = new ArrayList<>();
        
        // 外部存储根目录
        File externalStorage = Environment.getExternalStorageDirectory();
        directories.add(externalStorage);
        
        // DCIM目录
        File dcimDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if (dcimDir.exists()) directories.add(dcimDir);
        
        // 电影目录
        File moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        if (moviesDir.exists()) directories.add(moviesDir);
        
        // 下载目录
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (downloadDir.exists()) directories.add(downloadDir);
        
        // 图片目录
        File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (picturesDir.exists()) directories.add(picturesDir);
        
        return directories;
    }
    
    // 检查文件是否为视频文件
    public static boolean isVideoFile(String filename) {
        String[] videoExtensions = {".mp4", ".mkv", ".avi", ".mov", ".wmv", ".flv", ".webm", ".m4v", ".3gp"};
        filename = filename.toLowerCase();
        for (String ext : videoExtensions) {
            if (filename.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
    
    // 获取格式化的文件大小
    public static String getFormattedFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}
