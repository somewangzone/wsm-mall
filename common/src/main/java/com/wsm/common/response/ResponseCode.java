package com.wsm.common.response;

public enum ResponseCode {
    SUCCESS(200,"Request Successfully!"),
    BAD_REQUEST(400,"Bad request"),
    UNAUTHORIZED(401,"Unauthorized");

    private final Integer code;
    private final String message;

    ResponseCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
