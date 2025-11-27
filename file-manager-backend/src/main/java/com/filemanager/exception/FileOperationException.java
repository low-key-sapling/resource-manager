package com.filemanager.exception;

import lombok.Getter;

/**
 * 文件操作异常
 */
@Getter
public class FileOperationException extends RuntimeException {
    
    private final String errorCode;
    
    public FileOperationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public FileOperationException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
