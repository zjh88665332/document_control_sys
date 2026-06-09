package com.docmanage.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private Integer code;
    private String msg;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .msg("成功")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String msg, T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .msg(msg)
                .data(data)
                .build();
    }

    public static ApiResponse<Void> success(String msg) {
        return ApiResponse.<Void>builder()
                .code(200)
                .msg(msg)
                .build();
    }

    public static <T> ApiResponse<T> error(int code, String msg) {
        return ApiResponse.<T>builder()
                .code(code)
                .msg(msg)
                .build();
    }
}
