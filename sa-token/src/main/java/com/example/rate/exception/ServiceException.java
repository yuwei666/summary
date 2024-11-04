package com.example.rate.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ServiceException extends RuntimeException {

    private Integer code;

    private String message;

    private String detailMessage;

    public ServiceException(String message) {
        this.message = message;
    }
}
