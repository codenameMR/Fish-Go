package com.fishgo.common.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiResponse<T> {
    private List<String> message;
    private int status;
    private T data;

    // 생성자 - List<String> 타입 처리
    public ApiResponse(List<String> messages, int status) {
        this.message = messages;  // 메시지 리스트 그대로 받음
        this.status = status * 100;
        this.data = null;
    }

    // 생성자 - 단일 메시지도 List로 처리
    public ApiResponse(String message, int status) {
        this.message = List.of(message); // 단일 메시지를 List로 감싸서 전달
        this.status = status * 100;
        this.data = null;
    }

    // 생성자 - data 포함
    public ApiResponse(List<String> messages, int status, T data) {
        this.message = messages; // 메시지 리스트 그대로 받음
        this.status = status * 100;
        this.data = data;
    }

    // 생성자 - data 포함 및 메세지가 한 개일 경우
    public ApiResponse(String message, int status, T data) {
        this.message = List.of(message);;
        this.status = status * 100;
        this.data = data;
    }

}

