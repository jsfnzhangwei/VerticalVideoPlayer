package com.example.verticalvideoplayer.models;

import java.io.File;
import java.util.Date;

public class FileItem {
    private String name;
    private String path;
    private boolean isDirectory;
    private long size;
    private Date modifiedDate;
    private String serverType; // "local", "smb", "webdav"

    // 构造函数 - 本地文件
    public FileItem(File file) {
        this.name = file.getName();
        this.path = file.getAbsolutePath();
        this.isDirectory = file.isDirectory();
        this.size = file.length();
        this.modifiedDate = new Date(file.lastModified());
        this.serverType = "local";
    }

    // 构造函数 - 网络文件
    public FileItem(String name, String path, boolean isDirectory, long size, Date modifiedDate, String serverType) {
        this.name = name;
        this.path = path;
        this.isDirectory = isDirectory;
        this.size = size;
        this.modifiedDate = modifiedDate;
        this.serverType = serverType;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public long getSize() {
        return size;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public String getServerType() {
        return serverType;
    }

    // 获取文件扩展名
    public String getExtension() {
        if (isDirectory) return "";
        int lastDot = name.lastIndexOf('.');
        if (lastDot == -1) return "";
        return name.substring(lastDot + 1).toLowerCase();
    }

    // 检查是否为视频文件
    public boolean isVideoFile() {
        if (isDirectory) return false;
        String ext = getExtension();
        return ext.equals("mp4") || ext.equals("mkv") || ext.equals("avi") || 
               ext.equals("mov") || ext.equals("wmv") || ext.equals("flv") || 
               ext.equals("webm") || ext.equals("m4v") || ext.equals("3gp");
    }

    // 获取格式化的文件大小
    public String getFormattedSize() {
        if (isDirectory) return "";
        if (size < 1024) return size + " B";
        int exp = (int) (Math.log(size) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", size / Math.pow(1024, exp), pre);
    }
}
