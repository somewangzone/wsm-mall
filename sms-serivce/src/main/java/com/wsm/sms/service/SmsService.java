package com.wsm.sms.service;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.DescribePhoneNumberInfoRequest;
import com.tencentcloudapi.sms.v20210111.models.DescribePhoneNumberInfoResponse;
import com.tencentcloudapi.sms.v20210111.models.PhoneNumberInfo;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.wsm.sms.config.TencentSmsConfig;
import com.wsm.sms.processor.RedisCommonProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SmsService {

    @Autowired
    private TencentSmsConfig tencentSmsConfig;

    @Autowired
    private RedisCommonProcessor redisCommonProcessor;

    // 官方文档地址： https://cloud.tencent.com/document/product/382/43194
    public void sendSms(String phoneNumber) {
        try {
            Credential credential = new Credential(tencentSmsConfig.getSecretId(), tencentSmsConfig.getSecretKey());

            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setReqMethod("POST"); // get请求(默认为post请求)
            httpProfile.setConnTimeout(60); // 请求连接超时时间，单位为秒(默认60秒)

            /* 非必要步骤:
             * 实例化一个客户端配置对象，可以指定超时时间等配置 */
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);

            SmsClient client = new SmsClient(credential, tencentSmsConfig.getRegion(), clientProfile);
            SendSmsRequest req = new SendSmsRequest();
            req.setSmsSdkAppId(tencentSmsConfig.getAppId());
            req.setSignName(tencentSmsConfig.getSignName());
            req.setTemplateId(tencentSmsConfig.getTemplateId().getPhoneCode());

            String code = getRandomPhoneCode();
            String[] templateParamSet = {code};
            req.setTemplateParamSet(templateParamSet);

            // 前端需要添加+86
            String[] phoneNumberSet = {phoneNumber};
            req.setPhoneNumberSet(phoneNumberSet);

            client.SendSms(req);
            redisCommonProcessor.set(phoneNumber, code, 300L);
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException(e);
        }
    }

    // 可以通过这种方式查询phoneNumber的国际代码，没必要不要使用
    private Map getNationalCode(SmsClient smsClient, String phoneNumber) {
        DescribePhoneNumberInfoRequest request = new DescribePhoneNumberInfoRequest();
        // 这种不带国家编码的
        String[] phoneNumberSet = {phoneNumber};
        request.setPhoneNumberSet(phoneNumberSet);
        Map<String,String> mapResults = new HashMap<>();
        try {
            DescribePhoneNumberInfoResponse describePhoneNumberInfoResponse = smsClient.DescribePhoneNumberInfo(request);
            PhoneNumberInfo[] phoneNumberInfoSet = describePhoneNumberInfoResponse.getPhoneNumberInfoSet();
            for (PhoneNumberInfo phoneNumberInfo : phoneNumberInfoSet) {
                mapResults.put(phoneNumberInfo.getPhoneNumber(),phoneNumberInfo.getNationCode());
            }
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException(e);
        }
        return mapResults;
    }

    private String getRandomPhoneCode() {
        return String.valueOf((Math.random() * 9 + 1) * 100000);
    }
}
