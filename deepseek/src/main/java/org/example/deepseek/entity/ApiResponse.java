package org.example.deepseek.entity;

import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * 自定义响应体
 */
@Data
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(HttpStatus.OK.value());
        response.setMessage("success");
        response.setData(data);
        return response;
    }
}
