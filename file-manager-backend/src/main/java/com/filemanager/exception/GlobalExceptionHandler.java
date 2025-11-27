package com.filemanager.exception;

import com.filemanager.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理文件操作异常
     */
    @ExceptionHandler(FileOperationException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileOperationException(FileOperationException e) {
        log.error("文件操作异常: {} - {}", e.getErrorCode(), e.getMessage());
        
        HttpStatus status = switch (e.getErrorCode()) {
            case "FILE_NOT_FOUND", "DIRECTORY_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "PATH_TRAVERSAL" -> HttpStatus.FORBIDDEN;
            case "FILE_EXISTS" -> HttpStatus.CONFLICT;
            case "INVALID_NAME", "INVALID_PATH" -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        
        return ResponseEntity.status(status)
                .body(ApiResponse.error(e.getErrorCode(), e.getMessage()));
    }
    
    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("未知异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "服务器内部错误: " + e.getMessage()));
    }
}
