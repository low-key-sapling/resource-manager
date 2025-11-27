package com.filemanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件节点数据传输对象
 * 用于表示文件或目录的信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileNodeDTO {
    
    /**
     * 文件/目录名称
     */
    private String name;
    
    /**
     * 完整路径
     */
    private String path;
    
    /**
     * 类型: "file" 或 "directory"
     */
    private String type;
    
    /**
     * 文件扩展名 (仅文件有效)
     */
    private String extension;
    
    /**
     * 文件大小 (字节，仅文件有效)
     */
    private Long size;
    
    /**
     * 子节点列表 (仅目录有效)
     */
    private List<FileNodeDTO> children;
    
    /**
     * 最后修改时间
     */
    private LocalDateTime lastModified;
    
    /**
     * 判断是否为目录
     */
    public boolean isDirectory() {
        return "directory".equals(type);
    }
    
    /**
     * 判断是否为文件
     */
    public boolean isFile() {
        return "file".equals(type);
    }
}
