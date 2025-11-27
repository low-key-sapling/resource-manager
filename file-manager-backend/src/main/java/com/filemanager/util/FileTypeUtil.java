package com.filemanager.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 文件类型工具类
 * 根据扩展名识别文件的 MIME 类型
 */
public class FileTypeUtil {
    
    private static final Map<String, String> MIME_TYPES = new HashMap<>();
    private static final Set<String> TEXT_EXTENSIONS = Set.of(
            "md", "txt", "html", "htm", "css", "js", "ts", "json", 
            "xml", "yaml", "yml", "java", "py", "sql", "sh", "bat",
            "properties", "ini", "conf", "log", "csv"
    );
    
    static {
        // 文本文件
        MIME_TYPES.put("md", "text/markdown");
        MIME_TYPES.put("txt", "text/plain");
        MIME_TYPES.put("html", "text/html");
        MIME_TYPES.put("htm", "text/html");
        MIME_TYPES.put("css", "text/css");
        MIME_TYPES.put("js", "application/javascript");
        MIME_TYPES.put("ts", "application/typescript");
        MIME_TYPES.put("json", "application/json");
        MIME_TYPES.put("xml", "application/xml");
        MIME_TYPES.put("yaml", "text/yaml");
        MIME_TYPES.put("yml", "text/yaml");
        
        // 编程语言
        MIME_TYPES.put("java", "text/x-java-source");
        MIME_TYPES.put("py", "text/x-python");
        MIME_TYPES.put("sql", "application/sql");
        
        // 文档
        MIME_TYPES.put("pdf", "application/pdf");
        MIME_TYPES.put("doc", "application/msword");
        MIME_TYPES.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        MIME_TYPES.put("xls", "application/vnd.ms-excel");
        MIME_TYPES.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        
        // 图片
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("svg", "image/svg+xml");
    }
    
    /**
     * 根据扩展名获取 MIME 类型
     */
    public static String getMimeType(String extension) {
        if (extension == null || extension.isEmpty()) {
            return "application/octet-stream";
        }
        return MIME_TYPES.getOrDefault(extension.toLowerCase(), "application/octet-stream");
    }
    
    /**
     * 判断是否为文本文件
     */
    public static boolean isTextFile(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }
        return TEXT_EXTENSIONS.contains(extension.toLowerCase());
    }
    
    /**
     * 判断是否为可预览的文件类型
     */
    public static boolean isPreviewable(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }
        String ext = extension.toLowerCase();
        return TEXT_EXTENSIONS.contains(ext) 
                || ext.equals("pdf") 
                || ext.equals("doc") 
                || ext.equals("docx")
                || ext.equals("xls")
                || ext.equals("xlsx");
    }
}
