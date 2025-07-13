package com.excelfore.aws.awstask.exception;

import com.excelfore.aws.awstask.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private <T> ApiResponse<T> buildErrorResponse(Exception ex) {
        Map<String, Object> errorDetails = new LinkedHashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("errorCode", ex.getClass().getSimpleName());
        errorDetails.put("message", ex.getMessage());
        return new ApiResponse<>(ex.getClass().getSimpleName(), ex.getMessage());
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiResponse<Object>> handleFileUploadException(FileUploadException ex) {
        ApiResponse<Object> response = buildErrorResponse(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(FileDownloadException.class)
    public ResponseEntity<ApiResponse<Object>> handleFileDownloadException(FileDownloadException ex) {
        ApiResponse<Object> response = buildErrorResponse(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(FileTooLargeException.class)
    public ResponseEntity<ApiResponse<Object>> handleFileTooLarge(FileTooLargeException ex) {
        ApiResponse<Object> response = buildErrorResponse(ex);
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }

    @ExceptionHandler(FileAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleFileAlreadyExists(FileAlreadyExistsException ex) {
        ApiResponse<Object> response = buildErrorResponse(ex);
        return ResponseEntity.status(HttpStatus.FOUND).body(response);
    }

    @ExceptionHandler(NoSuchFilePresent.class)
    public ResponseEntity<ApiResponse<Object>> handleNoSuchFilePresent(NoSuchFilePresent ex) {
        ApiResponse<Object> response = buildErrorResponse(ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(HashMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleHashMismatchException(HashMismatchException ex) {
        ApiResponse<Object> response = buildErrorResponse(ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(PresignedUrlExpiredException.class)
    public ResponseEntity<ApiResponse<Object>> handlePresignedUrlExpired(PresignedUrlExpiredException ex) {
        ApiResponse<Object> response = buildErrorResponse(ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        ApiResponse<Object> response = buildErrorResponse(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
