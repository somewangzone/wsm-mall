package com.wsm.common.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommonResponse {
    private Integer code;
    private Object content;
    private String message;
}
