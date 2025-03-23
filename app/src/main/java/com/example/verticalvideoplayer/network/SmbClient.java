package com.example.verticalvideoplayer.network;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.verticalvideoplayer.models.FileItem;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class SmbClient {
    private static final String TAG = "SmbClient";
    private final Executor executor = Executors.newSingleThreadExecutor();
    
    private String serverAddress;
    private String username;
    private String password;
    private String domain = "";
    private NtlmPasswordAuthentication auth;
    
    // 连接到SMB服务器
    public LiveData<Boolean> connect(String serverAddress, String username, String password) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        this.serverAddress = serverAddress;
        this.username = username;
        this.password = password;
        
        // 解析域名（如果有）
        if (username.contains("\\")) {
            String[] parts = username.split("\\\\");
            this.domain = parts[0];
            this.username = parts[1];
        }
        
        // 创建认证对象
        this.auth = new NtlmPasswordAuthentication(domain, this.username, password);
        
        executor.execute(() -> {
            try {
                // 验证连接
                String smbUrl = formatSmbUrl(serverAddress, "");
                SmbFile smbFile = new SmbFile(smbUrl, auth);
                smbFile.exists(); // 尝试访问以验证连接
                
                result.postValue(true);
            } catch (Exception e) {
                Log.e(TAG, "SMB连接失败: " + e.getMessage());
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
                String smbUrl = formatSmbUrl(serverAddress, path);
                SmbFile smbFile = new SmbFile(smbUrl, auth);
                
                if (!smbFile.exists() || !smbFile.isDirectory()) {
                    result.postValue(new ArrayList<>());
                    return;
                }
                
                SmbFile[] files = smbFile.listFiles();
                List<FileItem> fileItems = new ArrayList<>();
                
                for (SmbFile file : files) {
                    String name = file.getName();
                    // 移除末尾的斜杠（如果有）
                    if (name.endsWith("/")) {
                        name = name.substring(0, name.length() - 1);
                    }
                    
                    // 创建FileItem对象
                    FileItem item = new FileItem(
                            name,
                            file.getPath(),
                            file.isDirectory(),
                            file.length(),
                            new Date(file.getLastModified()),
                            "smb"
                    );
                    
                    fileItems.add(item);
                }
                
                result.postValue(fileItems);
            } catch (MalformedURLException e) {
                Log.e(TAG, "SMB URL格式错误: " + e.getMessage());
                e.printStackTrace();
                result.postValue(new ArrayList<>());
            } catch (SmbException e) {
                Log.e(TAG, "SMB访问错误: " + e.getMessage());
                e.printStackTrace();
                result.postValue(new ArrayList<>());
            } catch (IOException e) {
                Log.e(TAG, "SMB IO错误: " + e.getMessage());
                e.printStackTrace();
                result.postValue(new ArrayList<>());
            }
        });
        
        return result;
    }
    
    // 格式化SMB URL
    private String formatSmbUrl(String serverAddress, String path) {
        // 确保服务器地址以smb://开头
        if (!serverAddress.startsWith("smb://")) {
            serverAddress = "smb://" + serverAddress;
        }
        
        // 确保服务器地址以/结尾
        if (!serverAddress.endsWith("/")) {
            serverAddress = serverAddress + "/";
        }
        
        // 如果路径不为空，确保不以/开头
        if (path != null && !path.isEmpty()) {
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
        }
        
        return serverAddress + path;
    }
    
    // 获取文件的完整SMB URL
    public String getFileUrl(String path) {
        return formatSmbUrl(serverAddress, path);
    }
}
