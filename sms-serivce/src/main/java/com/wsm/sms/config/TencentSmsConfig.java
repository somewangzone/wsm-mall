package com.wsm.sms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tencent.sms")
public class TencentSmsConfig {

    private String secretId;
    private String secretKey;
    private String region;
    private String appId;
    private String signName;
    private TemplateId templateId;

    @Data
    public static class TemplateId {
        private String phoneCode;
        private String sales;
    }
}
