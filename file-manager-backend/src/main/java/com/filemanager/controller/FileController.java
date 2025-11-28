package com.filemanager.controller;

import com.filemanager.dto.ApiResponse;
import com.filemanager.dto.FileContentDTO;
import com.filemanager.dto.FileNodeDTO;
import com.filemanager.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Map;

/**
 * 文件管理 REST API 控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    
    private final FileService fileService;
    
    /**
     * 获取目录树结构
     * GET /api/files/tree?path={path}
     */
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<FileNodeDTO>> getDirectoryTree(
            @RequestParam(defaultValue = "/") String path) {
        log.info("获取目录树: {}", path);
        FileNodeDTO tree = fileService.getDirectoryTree(path);
        return ResponseEntity.ok(ApiResponse.success(tree));
    }
    
    /**
     * 获取文件内容
     * GET /api/files/content?path={path}
     */
    @GetMapping("/content")
    public ResponseEntity<ApiResponse<FileContentDTO>> getFileContent(
            @RequestParam String path) {
        log.info("读取文件内容: {}", path);
        FileContentDTO content = fileService.readFileContent(path);
        return ResponseEntity.ok(ApiResponse.success(content));
    }
    
    /**
     * 保存文件内容
     * PUT /api/files/content
     */
    @PutMapping("/content")
    public ResponseEntity<ApiResponse<Void>> saveFileContent(
            @RequestBody Map<String, String> request) {
        String path = request.get("path");
        String content = request.get("content");
        log.info("保存文件内容: {}", path);
        fileService.saveFileContent(path, content);
        return ResponseEntity.ok(ApiResponse.success(null, "文件保存成功"));
    }
    
    /**
     * 创建目录
     * POST /api/files/directory
     */
    @PostMapping("/directory")
    public ResponseEntity<ApiResponse<Void>> createDirectory(
            @RequestBody Map<String, String> request) {
        String path = request.get("path");
        log.info("创建目录: {}", path);
        fileService.createDirectory(path);
        return ResponseEntity.ok(ApiResponse.success(null, "目录创建成功"));
    }
    
    /**
     * 创建文件
     * POST /api/files/file
     */
    @PostMapping("/file")
    public ResponseEntity<ApiResponse<Void>> createFile(
            @RequestBody Map<String, String> request) {
        String path = request.get("path");
        String content = request.getOrDefault("content", "");
        log.info("创建文件: {}", path);
        fileService.createFile(path, content);
        return ResponseEntity.ok(ApiResponse.success(null, "文件创建成功"));
    }
    
    /**
     * 检查文件或目录是否存在
     * GET /api/files/exists?path={path}
     */
    @GetMapping("/exists")
    public ResponseEntity<ApiResponse<Boolean>> checkExists(
            @RequestParam String path) {
        log.info("检查是否存在: {}", path);
        boolean exists = fileService.exists(path);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }
    
    /**
     * 下载/流式传输文件（用于PDF等二进制文件）
     * GET /api/files/download?path={path}
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String path) {
        log.info("下载文件: {}", path);
        try {
            Path filePath = fileService.getFilePath(path);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            
            String contentType = "application/octet-stream";
            String filename = filePath.getFileName().toString();
            String lowerFilename = filename.toLowerCase();
            
            // 根据文件扩展名设置Content-Type
            if (lowerFilename.endsWith(".pdf")) {
                contentType = "application/pdf";
            } else if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (lowerFilename.endsWith(".png")) {
                contentType = "image/png";
            } else if (lowerFilename.endsWith(".gif")) {
                contentType = "image/gif";
            } else if (lowerFilename.endsWith(".bmp")) {
                contentType = "image/bmp";
            } else if (lowerFilename.endsWith(".webp")) {
                contentType = "image/webp";
            } else if (lowerFilename.endsWith(".svg")) {
                contentType = "image/svg+xml";
            } else if (lowerFilename.endsWith(".ico")) {
                contentType = "image/x-icon";
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            log.error("文件路径错误: {}", path, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取当前根目录路径
     * GET /api/files/config/root-path
     */
    @GetMapping("/config/root-path")
    public ResponseEntity<ApiResponse<String>> getRootPath() {
        log.info("获取根目录路径");
        String rootPath = fileService.getRootPath();
        return ResponseEntity.ok(ApiResponse.success(rootPath));
    }
    
    /**
     * 设置根目录路径
     * PUT /api/files/config/root-path
     */
    @PutMapping("/config/root-path")
    public ResponseEntity<ApiResponse<Void>> setRootPath(@RequestBody Map<String, String> request) {
        String rootPath = request.get("rootPath");
        log.info("设置根目录路径: {}", rootPath);
        fileService.setRootPath(rootPath);
        return ResponseEntity.ok(ApiResponse.success(null, "根目录设置成功"));
    }
}
