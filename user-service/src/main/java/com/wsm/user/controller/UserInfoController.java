package com.wsm.user.controller;

import com.wsm.common.response.CommonResponse;
import com.wsm.user.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/info")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    @RequestMapping("/check-phone-bind-status")
    public CommonResponse checkPhoneBindStatus(@RequestHeader String personId) {

        return userInfoService.checkPhoneBindStatus(personId);
    }

    @RequestMapping("/bind-phone-number")
    public CommonResponse bindPhoneNumber(@RequestHeader String personId,
                                          @RequestParam String phoneNumber,
                                          @RequestParam String code){
        return userInfoService.bindPhoneNumber(personId,phoneNumber,code);
    }

    @RequestMapping("/get-by-token")
    public CommonResponse getUserInfoByToken(@RequestParam String token){
        return userInfoService.getUserInfoByToken(token);
    }
}
