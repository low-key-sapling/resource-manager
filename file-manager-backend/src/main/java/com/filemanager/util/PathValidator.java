package com.filemanager.util;

import com.filemanager.exception.FileOperationException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 路径验证工具类
 * 用于验证路径安全性和名称有效性
 */
public class PathValidator {
    
    // 路径遍历模式
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile("(^|[/\\\\])\\.\\.[/\\\\]?");
    
    // Windows 非法字符
    private static final Set<Character> INVALID_CHARS = Set.of(
            '<', '>', ':', '"', '|', '?', '*'
    );
    
    // 保留名称 (Windows)
    private static final Set<String> RESERVED_NAMES = Set.of(
            "CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
    );
    
    /**
     * 验证路径是否安全（防止路径遍历攻击）
     */
    public static void validatePath(String path, String rootPath) {
        if (path == null) {
            return;
        }
        
        // 检查路径遍历模式
        if (PATH_TRAVERSAL_PATTERN.matcher(path).find()) {
            throw new FileOperationException("PATH_TRAVERSAL", "检测到路径遍历攻击: " + path);
        }
        
        // 检查解析后的路径是否在根目录内
        try {
            Path root = Paths.get(rootPath).toAbsolutePath().normalize();
            Path resolved = root.resolve(path.startsWith("/") ? path.substring(1) : path)
                    .toAbsolutePath().normalize();
            
            if (!resolved.startsWith(root)) {
                throw new FileOperationException("PATH_TRAVERSAL", "路径超出允许范围: " + path);
            }
        } catch (Exception e) {
            if (e instanceof FileOperationException) {
                throw e;
            }
            throw new FileOperationException("INVALID_PATH", "无效路径: " + path);
        }
    }
    
    /**
     * 验证文件/目录名称是否有效
     */
    public static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new FileOperationException("INVALID_NAME", "名称不能为空");
        }
        
        String trimmedName = name.trim();
        
        // 检查是否只包含空白字符
        if (trimmedName.isEmpty()) {
            throw new FileOperationException("INVALID_NAME", "名称不能只包含空白字符");
        }
        
        // 检查非法字符
        for (char c : trimmedName.toCharArray()) {
            if (INVALID_CHARS.contains(c)) {
                throw new FileOperationException("INVALID_NAME", "名称包含非法字符: " + c);
            }
        }
        
        // 检查保留名称
        String upperName = trimmedName.toUpperCase();
        int dotIndex = upperName.indexOf('.');
        String baseName = dotIndex > 0 ? upperName.substring(0, dotIndex) : upperName;
        if (RESERVED_NAMES.contains(baseName)) {
            throw new FileOperationException("INVALID_NAME", "名称是系统保留名称: " + name);
        }
        
        // 检查是否以点或空格结尾
        if (trimmedName.endsWith(".") || trimmedName.endsWith(" ")) {
            throw new FileOperationException("INVALID_NAME", "名称不能以点或空格结尾");
        }
    }
    
    /**
     * 检查路径是否包含路径遍历模式
     */
    public static boolean containsPathTraversal(String path) {
        if (path == null) {
            return false;
        }
        return PATH_TRAVERSAL_PATTERN.matcher(path).find();
    }
}
