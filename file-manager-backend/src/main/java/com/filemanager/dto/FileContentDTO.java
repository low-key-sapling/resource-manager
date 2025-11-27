package com.filemanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件内容数据传输对象
 * 用于传输文件的内容信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileContentDTO {
    
    /**
     * 文件路径
     */
    private String path;
    
    /**
     * 文件内容
     */
    private String content;
    
    /**
     * 文件编码
     */
    private String encoding;
    
    /**
     * MIME 类型
     */
    private String mimeType;
}
