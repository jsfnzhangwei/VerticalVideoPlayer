package com.example.verticalvideoplayer.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.verticalvideoplayer.models.FileItem;
import com.example.verticalvideoplayer.network.SmbClient;

import java.util.List;

public class SmbViewModel extends ViewModel {
    
    private final SmbClient smbClient;
    private String currentPath = "";
    
    public SmbViewModel() {
        smbClient = new SmbClient();
    }
    
    // 连接到SMB服务器
    public LiveData<Boolean> connectToServer(String serverAddress, String username, String password) {
        return smbClient.connect(serverAddress, username, password);
    }
    
    // 列出目录内容
    public LiveData<List<FileItem>> listFiles(String path) {
        currentPath = path;
        return smbClient.listFiles(path);
    }
    
    // 获取当前路径
    public String getCurrentPath() {
        return currentPath;
    }
    
    // 获取文件的完整SMB URL
    public String getFileUrl(String path) {
        return smbClient.getFileUrl(path);
    }
}
