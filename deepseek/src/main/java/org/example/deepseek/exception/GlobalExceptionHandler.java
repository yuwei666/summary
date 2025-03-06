package org.example.deepseek.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<String> handleApiError(HttpClientErrorException e) {
        e.printStackTrace();
        return ResponseEntity.status(e.getStatusCode())
                .body("DeepSeek API 调用失败: " + e.getMessage());
    }
}