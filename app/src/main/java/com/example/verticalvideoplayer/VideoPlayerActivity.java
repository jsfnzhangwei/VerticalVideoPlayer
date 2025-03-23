package com.example.verticalvideoplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.verticalvideoplayer.viewmodels.SmbViewModel;
import com.example.verticalvideoplayer.viewmodels.VideoPlayerViewModel;
import com.example.verticalvideoplayer.viewmodels.WebDavViewModel;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;

import java.io.File;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

public class VideoPlayerActivity extends AppCompatActivity {

    private VideoPlayerViewModel viewModel;
    private SmbViewModel smbViewModel;
    private WebDavViewModel webDavViewModel;
    private PlayerView playerView;
    private ExoPlayer player;
    private TextView speedIndicator;
    private Switch autoPlaySwitch;
    
    private boolean isLongPressed = false;
    private String filePath;
    private int currentSortMethod;
    private String serverType = null;
    private String serverAddress;
    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        
        // 初始化视图
        playerView = findViewById(R.id.playerView);
        speedIndicator = findViewById(R.id.speedIndicator);
        autoPlaySwitch = findViewById(R.id.autoPlaySwitch);
        
        // 获取传递的参数
        filePath = getIntent().getStringExtra("filePath");
        currentSortMethod = getIntent().getIntExtra("currentSortMethod", VideoPlayerViewModel.SORT_NAME_ASC);
        serverType = getIntent().getStringExtra("serverType");
        
        if (serverType != null) {
            serverAddress = getIntent().getStringExtra("serverAddress");
            username = getIntent().getStringExtra("username");
            password = getIntent().getStringExtra("password");
        }
        
        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(VideoPlayerViewModel.class);
        viewModel.setCurrentSortMethod(currentSortMethod);
        
        if (ServerConnectionActivity.SERVER_TYPE_SMB.equals(serverType)) {
            smbViewModel = new ViewModelProvider(this).get(SmbViewModel.class);
        } else if (ServerConnectionActivity.SERVER_TYPE_WEBDAV.equals(serverType)) {
            webDavViewModel = new ViewModelProvider(this).get(WebDavViewModel.class);
        }
        
        // 初始化播放器
        initializePlayer();
        
        // 设置自动播放开关
        autoPlaySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setAutoPlayEnabled(isChecked);
        });
        
        // 设置长按监听，实现长按2倍速快进功能
        playerView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 开始长按检测
                    v.postDelayed(() -> {
                        isLongPressed = true;
                        setPlaybackSpeed(2.0f);
                        speedIndicator.setVisibility(View.VISIBLE);
                    }, 500); // 500毫秒长按阈值
                    break;
                    
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // 取消长按检测
                    v.removeCallbacksAndMessages(null);
                    if (isLongPressed) {
                        setPlaybackSpeed(1.0f);
                        speedIndicator.setVisibility(View.GONE);
                        isLongPressed = false;
                    } else {
                        // 如果不是长按，则切换播放/暂停
                        togglePlayPause();
                    }
                    break;
            }
            return true;
        });
    }
    
    private void initializePlayer() {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        
        try {
            // 设置媒体源
            MediaItem mediaItem;
            
            if (ServerConnectionActivity.SERVER_TYPE_SMB.equals(serverType)) {
                // 处理SMB文件
                String domain = "";
                String user = username;
                
                // 解析域名（如果有）
                if (username != null && username.contains("\\")) {
                    String[] parts = username.split("\\\\");
                    domain = parts[0];
                    user = parts[1];
                }
                
                // 创建SMB认证
                NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(domain, user, password);
                
                // 创建SMB URL
                String smbUrl = filePath;
                if (!smbUrl.startsWith("smb://")) {
                    smbUrl = "smb://" + serverAddress + "/" + filePath;
                }
                
                // 创建MediaItem
                mediaItem = MediaItem.fromUri(Uri.parse(smbUrl));
            } else if (ServerConnectionActivity.SERVER_TYPE_WEBDAV.equals(serverType)) {
                // 处理WebDAV文件
                // 创建WebDAV URL
                String webDavUrl = webDavViewModel.getFileUrl(filePath);
                
                // 创建带认证的MediaItem
                mediaItem = MediaItem.fromUri(Uri.parse(webDavUrl));
                
                // 注意：ExoPlayer可能需要额外的认证处理来播放WebDAV文件
                // 这里简化处理，实际应用中可能需要更复杂的认证机制
            } else {
                // 处理本地文件
                mediaItem = MediaItem.fromUri(Uri.parse(filePath));
            }
            
            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();
            
            // 监听播放完成事件
            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_ENDED) {
                        if (viewModel.isAutoPlayEnabled()) {
                            // 自动播放下一个视频
                            playNextVideo();
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void playNextVideo() {
        if (serverType != null) {
            // 网络文件的下一个视频逻辑
            // 这部分将在后续完善
            finish();
        } else {
            // 本地文件的下一个视频
            String nextFilePath = viewModel.getNextVideoFile(filePath);
            if (nextFilePath != null) {
                Intent intent = new Intent(VideoPlayerActivity.this, VideoPlayerActivity.class);
                intent.putExtra("filePath", nextFilePath);
                intent.putExtra("currentSortMethod", currentSortMethod);
                startActivity(intent);
                finish();
            }
        }
    }
    
    private void setPlaybackSpeed(float speed) {
        if (player != null) {
            PlaybackParameters params = new PlaybackParameters(speed);
            player.setPlaybackParameters(params);
        }
    }
    
    private void togglePlayPause() {
        if (player != null) {
            if (player.isPlaying()) {
                player.pause();
            } else {
                player.play();
            }
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
