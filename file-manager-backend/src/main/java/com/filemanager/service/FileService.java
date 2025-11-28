package com.filemanager.service;

import com.filemanager.dto.FileContentDTO;
import com.filemanager.dto.FileNodeDTO;

/**
 * 文件服务接口
 * 定义文件操作的核心方法
 */
public interface FileService {
    
    /**
     * 获取目录树结构
     * @param path 起始路径
     * @return 目录树节点
     */
    FileNodeDTO getDirectoryTree(String path);
    
    /**
     * 读取文件内容
     * @param path 文件路径
     * @return 文件内容
     */
    FileContentDTO readFileContent(String path);
    
    /**
     * 保存文件内容
     * @param path 文件路径
     * @param content 文件内容
     */
    void saveFileContent(String path, String content);
    
    /**
     * 创建目录
     * @param path 目录路径
     */
    void createDirectory(String path);
    
    /**
     * 创建文件
     * @param path 文件路径
     * @param content 初始内容
     */
    void createFile(String path, String content);
    
    /**
     * 检查文件或目录是否存在
     * @param path 路径
     * @return 是否存在
     */
    boolean exists(String path);
    
    /**
     * 获取文件的完整路径
     * @param path 相对路径
     * @return 完整的Path对象
     */
    java.nio.file.Path getFilePath(String path);
    
    /**
     * 获取当前根目录路径
     * @return 根目录路径
     */
    String getRootPath();
    
    /**
     * 设置根目录路径
     * @param rootPath 新的根目录路径
     */
    void setRootPath(String rootPath);
}
