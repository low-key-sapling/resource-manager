package com.filemanager.service;

import com.filemanager.dto.FileContentDTO;
import com.filemanager.dto.FileNodeDTO;
import com.filemanager.exception.FileOperationException;
import com.filemanager.util.FileTypeUtil;
import com.filemanager.util.PathValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * 文件服务实现类
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {
    
    @Value("${file-manager.root-path:./managed-files}")
    private String rootPath;
    
    @Override
    public FileNodeDTO getDirectoryTree(String path) {
        Path resolvedPath = resolvePath(path);
        PathValidator.validatePath(path, rootPath);
        
        if (!Files.exists(resolvedPath)) {
            throw new FileOperationException("DIRECTORY_NOT_FOUND", "目录不存在: " + path);
        }
        
        return buildFileNode(resolvedPath, path);
    }
    
    @Override
    public FileContentDTO readFileContent(String path) {
        Path resolvedPath = resolvePath(path);
        PathValidator.validatePath(path, rootPath);
        
        if (!Files.exists(resolvedPath)) {
            throw new FileOperationException("FILE_NOT_FOUND", "文件不存在: " + path);
        }
        
        if (Files.isDirectory(resolvedPath)) {
            throw new FileOperationException("INVALID_PATH", "路径是目录而非文件: " + path);
        }

        try {
            String content = Files.readString(resolvedPath, StandardCharsets.UTF_8);
            String extension = getExtension(path);
            
            return FileContentDTO.builder()
                    .path(path)
                    .content(content)
                    .encoding("UTF-8")
                    .mimeType(FileTypeUtil.getMimeType(extension))
                    .build();
        } catch (IOException e) {
            log.error("读取文件失败: {}", path, e);
            throw new FileOperationException("FILE_READ_ERROR", "读取文件失败: " + e.getMessage());
        }
    }
    
    @Override
    public void saveFileContent(String path, String content) {
        Path resolvedPath = resolvePath(path);
        PathValidator.validatePath(path, rootPath);
        
        if (!Files.exists(resolvedPath)) {
            throw new FileOperationException("FILE_NOT_FOUND", "文件不存在: " + path);
        }
        
        try {
            Files.writeString(resolvedPath, content, StandardCharsets.UTF_8);
            log.info("文件保存成功: {}", path);
        } catch (IOException e) {
            log.error("保存文件失败: {}", path, e);
            throw new FileOperationException("FILE_WRITE_ERROR", "保存文件失败: " + e.getMessage());
        }
    }
    
    @Override
    public void createDirectory(String path) {
        Path resolvedPath = resolvePath(path);
        PathValidator.validatePath(path, rootPath);
        PathValidator.validateName(resolvedPath.getFileName().toString());
        
        if (Files.exists(resolvedPath)) {
            throw new FileOperationException("FILE_EXISTS", "目录已存在: " + path);
        }
        
        try {
            Files.createDirectories(resolvedPath);
            log.info("目录创建成功: {}", path);
        } catch (IOException e) {
            log.error("创建目录失败: {}", path, e);
            throw new FileOperationException("DIRECTORY_CREATE_ERROR", "创建目录失败: " + e.getMessage());
        }
    }
    
    @Override
    public void createFile(String path, String content) {
        Path resolvedPath = resolvePath(path);
        PathValidator.validatePath(path, rootPath);
        PathValidator.validateName(resolvedPath.getFileName().toString());
        
        try {
            Path parent = resolvedPath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            Files.writeString(resolvedPath, content != null ? content : "", StandardCharsets.UTF_8);
            log.info("文件创建成功: {}", path);
        } catch (IOException e) {
            log.error("创建文件失败: {}", path, e);
            throw new FileOperationException("FILE_CREATE_ERROR", "创建文件失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean exists(String path) {
        Path resolvedPath = resolvePath(path);
        PathValidator.validatePath(path, rootPath);
        return Files.exists(resolvedPath);
    }
    
    @Override
    public Path getFilePath(String path) {
        Path resolvedPath = resolvePath(path);
        PathValidator.validatePath(path, rootPath);
        return resolvedPath;
    }
    
    @Override
    public String getRootPath() {
        return rootPath;
    }
    
    @Override
    public void setRootPath(String newRootPath) {
        Path path = Paths.get(newRootPath);
        if (!Files.exists(path)) {
            throw new FileOperationException("INVALID_PATH", "目录不存在: " + newRootPath);
        }
        if (!Files.isDirectory(path)) {
            throw new FileOperationException("INVALID_PATH", "路径不是目录: " + newRootPath);
        }
        this.rootPath = newRootPath;
        log.info("根目录已更新为: {}", newRootPath);
    }

    
    /**
     * 构建文件节点（递归）
     */
    private FileNodeDTO buildFileNode(Path filePath, String relativePath) {
        try {
            boolean isDirectory = Files.isDirectory(filePath);
            String name = filePath.getFileName() != null ? filePath.getFileName().toString() : "";
            
            FileNodeDTO.FileNodeDTOBuilder builder = FileNodeDTO.builder()
                    .name(name)
                    .path(relativePath)
                    .type(isDirectory ? "directory" : "file")
                    .lastModified(LocalDateTime.ofInstant(
                            Files.getLastModifiedTime(filePath).toInstant(),
                            ZoneId.systemDefault()));
            
            if (isDirectory) {
                List<FileNodeDTO> children = new ArrayList<>();
                try (Stream<Path> stream = Files.list(filePath)) {
                    stream.forEach(child -> {
                        String childRelativePath = relativePath.endsWith("/") 
                                ? relativePath + child.getFileName()
                                : relativePath + "/" + child.getFileName();
                        children.add(buildFileNode(child, childRelativePath));
                    });
                }
                // 排序：目录优先，然后按字母顺序
                sortFileNodes(children);
                builder.children(children);
            } else {
                builder.extension(getExtension(name));
                builder.size(Files.size(filePath));
            }
            
            return builder.build();
        } catch (IOException e) {
            log.error("构建文件节点失败: {}", filePath, e);
            throw new FileOperationException("FILE_READ_ERROR", "读取文件信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 排序文件节点：目录优先，字母顺序
     */
    public static void sortFileNodes(List<FileNodeDTO> nodes) {
        nodes.sort(Comparator
                .comparing(FileNodeDTO::isDirectory).reversed()
                .thenComparing(node -> node.getName().toLowerCase()));
    }
    
    /**
     * 解析相对路径为绝对路径
     */
    private Path resolvePath(String path) {
        if (path == null || path.isEmpty() || path.equals("/")) {
            return Paths.get(rootPath);
        }
        String cleanPath = path.startsWith("/") ? path.substring(1) : path;
        return Paths.get(rootPath, cleanPath);
    }
    
    /**
     * 获取文件扩展名
     */
    private String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }
}
