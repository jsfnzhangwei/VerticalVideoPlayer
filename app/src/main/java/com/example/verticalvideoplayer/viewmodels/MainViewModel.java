package com.example.verticalvideoplayer.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.verticalvideoplayer.models.FileItem;
import com.example.verticalvideoplayer.utils.FileUtils;
import com.example.verticalvideoplayer.utils.SortUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class MainViewModel extends ViewModel {

    // 使用SortUtils中定义的常量
    public static final int SORT_NAME_ASC = SortUtils.SORT_NAME_ASC;
    public static final int SORT_NAME_DESC = SortUtils.SORT_NAME_DESC;
    public static final int SORT_DATE = SortUtils.SORT_DATE;
    public static final int SORT_NUMBER = SortUtils.SORT_NUMBER;

    private final MutableLiveData<List<FileItem>> fileList = new MutableLiveData<>();
    private final Stack<String> directoryStack = new Stack<>();
    private int currentSortMethod = SORT_NAME_ASC;
    private String currentPath;

    public MainViewModel() {
        fileList.setValue(new ArrayList<>());
    }

    public LiveData<List<FileItem>> getFileList() {
        return fileList;
    }

    public int getCurrentSortMethod() {
        return currentSortMethod;
    }

    // 加载本地文件
    public void loadLocalFiles() {
        // 默认从外部存储目录开始
        String path = "/storage/emulated/0";
        loadDirectory(path);
    }

    // 加载指定目录的文件
    public void loadDirectory(String path) {
        currentPath = path;
        File directory = new File(path);
        
        if (!directoryStack.isEmpty() && directoryStack.peek().equals(path)) {
            // 如果当前路径已经在栈顶，不做任何操作
            return;
        }
        
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }
        
        directoryStack.push(path);
        
        List<FileItem> items = new ArrayList<>();
        File[] files = directory.listFiles();
        
        if (files != null) {
            for (File file : files) {
                // 只添加目录和视频文件
                if (file.isDirectory() || FileUtils.isVideoFile(file.getName())) {
                    items.add(new FileItem(file));
                }
            }
        }
        
        // 使用SortUtils进行排序
        Collections.sort(items, SortUtils.getSortComparator(currentSortMethod));
        fileList.setValue(items);
    }

    // 导航到指定目录
    public void navigateToDirectory(String path) {
        loadDirectory(path);
    }

    // 返回上一级目录
    public void navigateUp() {
        if (directoryStack.size() > 1) {
            directoryStack.pop(); // 移除当前目录
            String previousPath = directoryStack.pop(); // 获取上一级目录并移除
            loadDirectory(previousPath); // 重新加载上一级目录
        }
    }

    // 检查是否为根目录
    public boolean isRootDirectory() {
        return directoryStack.size() <= 1;
    }

    // 排序文件列表
    public void sortFiles(int sortMethod) {
        currentSortMethod = sortMethod;
        List<FileItem> items = fileList.getValue();
        if (items != null) {
            // 使用SortUtils进行排序
            Collections.sort(items, SortUtils.getSortComparator(sortMethod));
            fileList.setValue(items);
        }
    }
    
    // 获取当前目录路径
    public String getCurrentPath() {
        return currentPath;
    }
    
    // 加载常用视频目录
    public void loadCommonVideoDirectories() {
        List<FileItem> items = new ArrayList<>();
        List<File> directories = FileUtils.getCommonVideoDirectories();
        
        for (File dir : directories) {
            items.add(new FileItem(dir));
        }
        
        // 使用SortUtils进行排序
        Collections.sort(items, SortUtils.getSortComparator(SORT_NAME_ASC));
        fileList.setValue(items);
    }
}
