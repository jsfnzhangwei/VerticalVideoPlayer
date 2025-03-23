package com.example.verticalvideoplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.verticalvideoplayer.viewmodels.ServerConnectionViewModel;
import com.example.verticalvideoplayer.viewmodels.SmbViewModel;
import com.example.verticalvideoplayer.viewmodels.WebDavViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class ServerConnectionActivity extends AppCompatActivity {

    private ServerConnectionViewModel viewModel;
    private SmbViewModel smbViewModel;
    private WebDavViewModel webDavViewModel;
    private RadioGroup serverTypeGroup;
    private RadioButton radioSmb;
    private RadioButton radioWebdav;
    private TextInputEditText serverAddress;
    private TextInputEditText username;
    private TextInputEditText password;
    private Button connectButton;
    
    public static final String EXTRA_SERVER_TYPE = "server_type";
    public static final String EXTRA_SERVER_ADDRESS = "server_address";
    public static final String EXTRA_USERNAME = "username";
    public static final String EXTRA_PASSWORD = "password";
    
    public static final String SERVER_TYPE_SMB = "smb";
    public static final String SERVER_TYPE_WEBDAV = "webdav";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_connection);
        
        // 设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // 初始化视图
        serverTypeGroup = findViewById(R.id.serverTypeGroup);
        radioSmb = findViewById(R.id.radioSmb);
        radioWebdav = findViewById(R.id.radioWebdav);
        serverAddress = findViewById(R.id.serverAddress);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        connectButton = findViewById(R.id.connectButton);
        
        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(ServerConnectionViewModel.class);
        smbViewModel = new ViewModelProvider(this).get(SmbViewModel.class);
        webDavViewModel = new ViewModelProvider(this).get(WebDavViewModel.class);
        
        // 设置连接按钮点击事件
        connectButton.setOnClickListener(v -> {
            String address = serverAddress.getText().toString().trim();
            String user = username.getText().toString().trim();
            String pass = password.getText().toString().trim();
            
            if (address.isEmpty()) {
                Toast.makeText(this, "请输入服务器地址", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 根据选择的服务器类型进行连接
            boolean isSmb = radioSmb.isChecked();
            
            connectButton.setEnabled(false);
            connectButton.setText("连接中...");
            
            if (isSmb) {
                // 使用SMB连接
                smbViewModel.connectToServer(address, user, pass)
                    .observe(this, success -> {
                        connectButton.setEnabled(true);
                        connectButton.setText(R.string.connect);
                        
                        if (success) {
                            Toast.makeText(this, "SMB连接成功", Toast.LENGTH_SHORT).show();
                            
                            // 创建Intent返回连接信息
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra(EXTRA_SERVER_TYPE, SERVER_TYPE_SMB);
                            resultIntent.putExtra(EXTRA_SERVER_ADDRESS, address);
                            resultIntent.putExtra(EXTRA_USERNAME, user);
                            resultIntent.putExtra(EXTRA_PASSWORD, pass);
                            
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else {
                            Toast.makeText(this, "SMB连接失败，请检查服务器地址和凭据", Toast.LENGTH_SHORT).show();
                        }
                    });
            } else {
                // 使用WebDAV连接
                webDavViewModel.connectToServer(address, user, pass)
                    .observe(this, success -> {
                        connectButton.setEnabled(true);
                        connectButton.setText(R.string.connect);
                        
                        if (success) {
                            Toast.makeText(this, "WebDAV连接成功", Toast.LENGTH_SHORT).show();
                            
                            // 创建Intent返回连接信息
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra(EXTRA_SERVER_TYPE, SERVER_TYPE_WEBDAV);
                            resultIntent.putExtra(EXTRA_SERVER_ADDRESS, address);
                            resultIntent.putExtra(EXTRA_USERNAME, user);
                            resultIntent.putExtra(EXTRA_PASSWORD, pass);
                            
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else {
                            Toast.makeText(this, "WebDAV连接失败，请检查服务器地址和凭据", Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
