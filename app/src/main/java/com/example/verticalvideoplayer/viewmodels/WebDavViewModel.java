package com.example.verticalvideoplayer.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.verticalvideoplayer.models.FileItem;
import com.example.verticalvideoplayer.network.WebDavClient;

import java.util.List;

public class WebDavViewModel extends ViewModel {
    
    private final WebDavClient webDavClient;
    private String currentPath = "";
    
    public WebDavViewModel() {
        webDavClient = new WebDavClient();
    }
    
    // 连接到WebDAV服务器
    public LiveData<Boolean> connectToServer(String serverUrl, String username, String password) {
        return webDavClient.connect(serverUrl, username, password);
    }
    
    // 列出目录内容
    public LiveData<List<FileItem>> listFiles(String path) {
        currentPath = path;
        return webDavClient.listFiles(path);
    }
    
    // 获取当前路径
    public String getCurrentPath() {
        return currentPath;
    }
    
    // 获取文件的完整WebDAV URL
    public String getFileUrl(String path) {
        return webDavClient.getFileUrl(path);
    }
}
