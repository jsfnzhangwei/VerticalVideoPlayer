package com.example.verticalvideoplayer.utils;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.verticalvideoplayer.models.FileItem;

public class SortUtils {
    
    public static final int SORT_NAME_ASC = 0;
    public static final int SORT_NAME_DESC = 1;
    public static final int SORT_DATE = 2;
    public static final int SORT_NUMBER = 3;
    
    // 获取排序比较器
    public static Comparator<FileItem> getSortComparator(int sortMethod) {
        switch (sortMethod) {
            case SORT_NAME_ASC:
                return getNameAscendingComparator();
            case SORT_NAME_DESC:
                return getNameDescendingComparator();
            case SORT_DATE:
                return getDateComparator();
            case SORT_NUMBER:
                return getNumberComparator();
            default:
                return getNameAscendingComparator();
        }
    }
    
    // 文件名升序比较器
    private static Comparator<FileItem> getNameAscendingComparator() {
        return (a, b) -> {
            // 目录优先
            if (a.isDirectory() && !b.isDirectory()) return -1;
            if (!a.isDirectory() && b.isDirectory()) return 1;
            // 按名称升序
            return a.getName().compareToIgnoreCase(b.getName());
        };
    }
    
    // 文件名降序比较器
    private static Comparator<FileItem> getNameDescendingComparator() {
        return (a, b) -> {
            // 目录优先
            if (a.isDirectory() && !b.isDirectory()) return -1;
            if (!a.isDirectory() && b.isDirectory()) return 1;
            // 按名称降序
            return b.getName().compareToIgnoreCase(a.getName());
        };
    }
    
    // 修改日期比较器（最新的在前面）
    private static Comparator<FileItem> getDateComparator() {
        return (a, b) -> {
            // 目录优先
            if (a.isDirectory() && !b.isDirectory()) return -1;
            if (!a.isDirectory() && b.isDirectory()) return 1;
            // 按修改日期降序
            return b.getModifiedDate().compareTo(a.getModifiedDate());
        };
    }
    
    // 数字递增比较器
    private static Comparator<FileItem> getNumberComparator() {
        return (a, b) -> {
            // 目录优先
            if (a.isDirectory() && !b.isDirectory()) return -1;
            if (!a.isDirectory() && b.isDirectory()) return 1;
            
            // 提取文件名中的数字
            Integer numA = extractNumber(a.getName());
            Integer numB = extractNumber(b.getName());
            
            // 如果两个文件名都包含数字，按数字大小排序
            if (numA != null && numB != null) {
                return numA.compareTo(numB);
            }
            
            // 如果只有一个文件名包含数字，将包含数字的排在前面
            if (numA != null) return -1;
            if (numB != null) return 1;
            
            // 如果都不包含数字，按名称排序
            return a.getName().compareToIgnoreCase(b.getName());
        };
    }
    
    // 从文件名中提取数字
    public static Integer extractNumber(String filename) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(filename);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    // 获取排序方法的显示名称
    public static String getSortMethodName(int sortMethod) {
        switch (sortMethod) {
            case SORT_NAME_ASC:
                return "文件名 (A-Z)";
            case SORT_NAME_DESC:
                return "文件名 (Z-A)";
            case SORT_DATE:
                return "修改日期";
            case SORT_NUMBER:
                return "数字递增";
            default:
                return "默认排序";
        }
    }
}
