package com.example.verticalvideoplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.verticalvideoplayer.adapters.FileAdapter;
import com.example.verticalvideoplayer.models.FileItem;
import com.example.verticalvideoplayer.viewmodels.MainViewModel;
import com.example.verticalvideoplayer.viewmodels.SmbViewModel;
import com.example.verticalvideoplayer.viewmodels.WebDavViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_SERVER_CONNECTION = 1001;
    
    private MainViewModel viewModel;
    private SmbViewModel smbViewModel;
    private WebDavViewModel webDavViewModel;
    private RecyclerView recyclerView;
    private FileAdapter adapter;
    private TextView emptyView;
    
    private String currentServerType = null;
    private String currentServerAddress = null;
    private String currentUsername = null;
    private String currentPassword = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // 初始化视图
        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);
        
        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FileAdapter(new ArrayList<>(), this::onFileItemClick);
        recyclerView.setAdapter(adapter);
        
        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        smbViewModel = new ViewModelProvider(this).get(SmbViewModel.class);
        webDavViewModel = new ViewModelProvider(this).get(WebDavViewModel.class);
        
        // 观察文件列表变化
        viewModel.getFileList().observe(this, fileItems -> {
            adapter.updateData(fileItems);
            updateEmptyView(fileItems);
        });
        
        // 设置添加服务器按钮
        FloatingActionButton fabAddServer = findViewById(R.id.fabAddServer);
        fabAddServer.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ServerConnectionActivity.class);
            startActivityForResult(intent, REQUEST_SERVER_CONNECTION);
        });
        
        // 加载本地文件
        viewModel.loadLocalFiles();
    }
    
    private void updateEmptyView(java.util.List<FileItem> fileItems) {
        if (fileItems.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_sort_name_asc) {
            viewModel.sortFiles(MainViewModel.SORT_NAME_ASC);
            return true;
        } else if (id == R.id.action_sort_name_desc) {
            viewModel.sortFiles(MainViewModel.SORT_NAME_DESC);
            return true;
        } else if (id == R.id.action_sort_date) {
            viewModel.sortFiles(MainViewModel.SORT_DATE);
            return true;
        } else if (id == R.id.action_sort_number) {
            viewModel.sortFiles(MainViewModel.SORT_NUMBER);
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void onFileItemClick(FileItem fileItem) {
        if (fileItem.isDirectory()) {
            // 如果是目录，进入该目录
            if (currentServerType != null) {
                if (currentServerType.equals(ServerConnectionActivity.SERVER_TYPE_SMB)) {
                    // 如果是SMB服务器，使用SMB浏览
                    smbViewModel.listFiles(fileItem.getPath()).observe(this, files -> {
                        adapter.updateData(files);
                        updateEmptyView(files);
                    });
                } else if (currentServerType.equals(ServerConnectionActivity.SERVER_TYPE_WEBDAV)) {
                    // 如果是WebDAV服务器，使用WebDAV浏览
                    webDavViewModel.listFiles(fileItem.getPath()).observe(this, files -> {
                        adapter.updateData(files);
                        updateEmptyView(files);
                    });
                }
            } else {
                // 否则使用本地文件浏览
                viewModel.navigateToDirectory(fileItem.getPath());
            }
        } else {
            // 如果是视频文件，打开播放器
            Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
            intent.putExtra("filePath", fileItem.getPath());
            intent.putExtra("currentSortMethod", viewModel.getCurrentSortMethod());
            
            // 添加服务器信息
            if (currentServerType != null) {
                intent.putExtra("serverType", currentServerType);
                intent.putExtra("serverAddress", currentServerAddress);
                intent.putExtra("username", currentUsername);
                intent.putExtra("password", currentPassword);
            }
            
            startActivity(intent);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_SERVER_CONNECTION && resultCode == RESULT_OK && data != null) {
            String serverType = data.getStringExtra(ServerConnectionActivity.EXTRA_SERVER_TYPE);
            String serverAddress = data.getStringExtra(ServerConnectionActivity.EXTRA_SERVER_ADDRESS);
            String username = data.getStringExtra(ServerConnectionActivity.EXTRA_USERNAME);
            String password = data.getStringExtra(ServerConnectionActivity.EXTRA_PASSWORD);
            
            // 保存服务器连接信息
            currentServerType = serverType;
            currentServerAddress = serverAddress;
            currentUsername = username;
            currentPassword = password;
            
            if (ServerConnectionActivity.SERVER_TYPE_SMB.equals(serverType)) {
                // 连接SMB服务器并浏览文件
                smbViewModel.connectToServer(serverAddress, username, password).observe(this, success -> {
                    if (success) {
                        // 连接成功，列出根目录文件
                        smbViewModel.listFiles("").observe(this, files -> {
                            adapter.updateData(files);
                            updateEmptyView(files);
                            
                            // 更新标题
                            getSupportActionBar().setTitle("SMB: " + serverAddress);
                        });
                    }
                });
            } else if (ServerConnectionActivity.SERVER_TYPE_WEBDAV.equals(serverType)) {
                // 连接WebDAV服务器并浏览文件
                webDavViewModel.connectToServer(serverAddress, username, password).observe(this, success -> {
                    if (success) {
                        // 连接成功，列出根目录文件
                        webDavViewModel.listFiles("").observe(this, files -> {
                            adapter.updateData(files);
                            updateEmptyView(files);
                            
                            // 更新标题
                            getSupportActionBar().setTitle("WebDAV: " + serverAddress);
                        });
                    } else {
                        // 连接失败
                        emptyView.setText("WebDAV连接失败，请检查服务器地址和凭据");
                        emptyView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                });
            }
        }
    }
    
    @Override
    public void onBackPressed() {
        // 如果当前是网络浏览模式，返回本地文件浏览
        if (currentServerType != null) {
            currentServerType = null;
            currentServerAddress = null;
            currentUsername = null;
            currentPassword = null;
            
            viewModel.loadLocalFiles();
            getSupportActionBar().setTitle(R.string.app_name);
        } 
        // 如果当前是本地文件浏览模式，且不是根目录，返回上一级目录
        else if (!viewModel.isRootDirectory()) {
            viewModel.navigateUp();
        } else {
            super.onBackPressed();
        }
    }
}
