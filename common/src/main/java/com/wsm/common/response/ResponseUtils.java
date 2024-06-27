package com.wsm.common.response;

public class ResponseUtils {

    public static CommonResponse okResponse(Object content) {
        return CommonResponse.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .message(ResponseCode.SUCCESS.getMessage())
                .content(content)
                .build();
    }

    public static CommonResponse okResponse(Object content, String message) {
        return CommonResponse.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .message(message)
                .content(content)
                .build();
    }

    public static CommonResponse failResponse(Integer code, Object content, String message) {
        return CommonResponse.builder()
                .code(code)
                .message(message)
                .content(content)
                .build();
    }
}
