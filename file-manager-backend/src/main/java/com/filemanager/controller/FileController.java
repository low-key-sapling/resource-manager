package com.filemanager.controller;

import com.filemanager.dto.ApiResponse;
import com.filemanager.dto.FileContentDTO;
import com.filemanager.dto.FileNodeDTO;
import com.filemanager.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
