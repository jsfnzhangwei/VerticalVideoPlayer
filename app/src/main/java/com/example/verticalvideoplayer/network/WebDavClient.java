package com.example.verticalvideoplayer.network;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.verticalvideoplayer.models.FileItem;
import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class WebDavClient {
    private static final String TAG = "WebDavClient";
    private final Executor executor = Executors.newSingleThreadExecutor();
    
    private String serverUrl;
    private String username;
    private String password;
    private Sardine sardine;
    
    public WebDavClient() {
        sardine = new OkHttpSardine();
    }
    
    // 连接到WebDAV服务器
    public LiveData<Boolean> connect(String serverUrl, String username, String password) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        this.serverUrl = formatWebDavUrl(serverUrl);
        this.username = username;
        this.password = password;
        
        executor.execute(() -> {
            try {
                // 设置认证信息
                sardine.setCredentials(username, password);
                
                // 验证连接
                sardine.exists(this.serverUrl);
                
                result.postValue(true);
            } catch (Exception e) {
                Log.e(TAG, "WebDAV连接失败: " + e.getMessage());
                e.printStackTrace();
                result.postValue(false);
            }
        });
        
        return result;
    }
    
    // 列出目录内容
    public LiveData<List<FileItem>> listFiles(String path) {
        MutableLiveData<List<FileItem>> result = new MutableLiveData<>();
        
        executor.execute(() -> {
            try {
                String fullUrl = combinePath(serverUrl, path);
                
                // 确保URL以/结尾
                if (!fullUrl.endsWith("/")) {
                    fullUrl += "/";
                }
                
                List<DavResource> resources = sardine.list(fullUrl);
                List<FileItem> fileItems = new ArrayList<>();
                
                // 第一个资源通常是当前目录，跳过
                for (int i = 1; i < resources.size(); i++) {
                    DavResource resource = resources.get(i);
                    
                    // 创建FileItem对象
                    String name = resource.getName();
                    String resourcePath = combinePath(path, name);
                    boolean isDirectory = resource.isDirectory();
                    long size = resource.getContentLength();
                    Date modifiedDate = resource.getModified();
                    
                    FileItem item = new FileItem(
                            name,
                            resourcePath,
                            isDirectory,
                            size,
                            modifiedDate,
                            "webdav"
                    );
                    
                    fileItems.add(item);
                }
                
                result.postValue(fileItems);
            } catch (IOException e) {
                Log.e(TAG, "WebDAV列出文件失败: " + e.getMessage());
                e.printStackTrace();
                result.postValue(new ArrayList<>());
            }
        });
        
        return result;
    }
    
    // 格式化WebDAV URL
    private String formatWebDavUrl(String serverUrl) {
        // 确保URL以http://或https://开头
        if (!serverUrl.startsWith("http://") && !serverUrl.startsWith("https://")) {
            serverUrl = "http://" + serverUrl;
        }
        
        // 确保URL以/结尾
        if (!serverUrl.endsWith("/")) {
            serverUrl = serverUrl + "/";
        }
        
        return serverUrl;
    }
    
    // 组合路径
    private String combinePath(String basePath, String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return basePath;
        }
        
        // 移除相对路径开头的/
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        
        // 确保基础路径以/结尾
        if (!basePath.endsWith("/")) {
            basePath = basePath + "/";
        }
        
        return basePath + relativePath;
    }
    
    // 获取文件的完整WebDAV URL
    public String getFileUrl(String path) {
        return combinePath(serverUrl, path);
    }
}
