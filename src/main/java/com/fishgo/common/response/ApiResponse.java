package com.fishgo.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ApiResponse<T> {
    private String message;
    private int status;
    private T data;

    public ApiResponse(String message, int status){
        this.message = message;
        this.status = status;
    }
}

