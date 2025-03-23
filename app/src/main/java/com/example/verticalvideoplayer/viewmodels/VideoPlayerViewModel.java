package com.example.verticalvideoplayer.viewmodels;

import androidx.lifecycle.ViewModel;

import com.example.verticalvideoplayer.models.FileItem;
import com.example.verticalvideoplayer.utils.FileUtils;
import com.example.verticalvideoplayer.utils.SortUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VideoPlayerViewModel extends ViewModel {

    // 使用SortUtils中定义的常量
    public static final int SORT_NAME_ASC = SortUtils.SORT_NAME_ASC;
    public static final int SORT_NAME_DESC = SortUtils.SORT_NAME_DESC;
    public static final int SORT_DATE = SortUtils.SORT_DATE;
    public static final int SORT_NUMBER = SortUtils.SORT_NUMBER;

    private boolean autoPlayEnabled = false;
    private int currentSortMethod = SORT_NAME_ASC;

    public void setAutoPlayEnabled(boolean enabled) {
        this.autoPlayEnabled = enabled;
    }

    public boolean isAutoPlayEnabled() {
        return autoPlayEnabled;
    }

    public void setCurrentSortMethod(int sortMethod) {
        this.currentSortMethod = sortMethod;
    }

    // 获取下一个视频文件
    public String getNextVideoFile(String currentFilePath) {
        if (currentFilePath == null) return null;

        File currentFile = new File(currentFilePath);
        if (!currentFile.exists()) return null;

        File parentDir = currentFile.getParentFile();
        if (parentDir == null || !parentDir.exists()) return null;

        // 获取同一目录下的所有文件
        File[] files = parentDir.listFiles();
        if (files == null || files.length == 0) return null;

        // 筛选出视频文件
        List<FileItem> videoFiles = new ArrayList<>();
        for (File file : files) {
            if (!file.isDirectory() && FileUtils.isVideoFile(file.getName())) {
                videoFiles.add(new FileItem(file));
            }
        }

        if (videoFiles.isEmpty()) return null;

        // 根据当前排序方法对视频文件进行排序
        Collections.sort(videoFiles, SortUtils.getSortComparator(currentSortMethod));

        // 找到当前文件在排序后列表中的位置
        int currentIndex = -1;
        for (int i = 0; i < videoFiles.size(); i++) {
            if (videoFiles.get(i).getPath().equals(currentFilePath)) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex == -1 || currentIndex == videoFiles.size() - 1) {
            // 如果是最后一个文件，返回第一个文件（循环播放）
            return videoFiles.get(0).getPath();
        } else {
            // 否则返回下一个文件
            return videoFiles.get(currentIndex + 1).getPath();
        }
    }
}
