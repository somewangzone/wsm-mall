package com.wsm.user.controller;

import com.wsm.common.response.CommonResponse;
import com.wsm.user.pojo.Ouath2ClientRegister;
import com.wsm.user.pojo.User;
import com.wsm.user.service.UserRegisterLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user/register")
public class UserRegisterLoginController {

    @Autowired
    private UserRegisterLoginService userRegisterLoginService;

    @PostMapping("/name-password")
    public CommonResponse namePasswordRegister(@RequestBody User user) {

        return userRegisterLoginService.namePasswordRegister(user);
    }

    // 手机 + 验证码
    @PostMapping("/phone-code")
    public CommonResponse phoneCodeRegister(@RequestParam String phoneNumber,
                                            @RequestParam String code) {

        return userRegisterLoginService.phoneCodeRegister(phoneNumber, code);
    }

    // 第三方授权，gitee
    // 这个接口是 第三方平台调用咱们的，叫回调接口
    @RequestMapping("/gitee")
    public CommonResponse thirdPartGiteeCallback(HttpServletRequest request) {

        return userRegisterLoginService.thirdPartGiteeCallback(request);
    }

    @RequestMapping("/login")
    public CommonResponse login(@RequestParam String userName,
                                @RequestParam String password) {

        return userRegisterLoginService.login(userName, password);
    }

    @RequestMapping("/third-part-app/request")
    public CommonResponse thirdPartAppRequest(
            @RequestHeader String personId,
            @RequestBody Ouath2ClientRegister ouath2ClientRegister) {
        return userRegisterLoginService.thirdPartAppRequest(personId, ouath2ClientRegister);
    }

    @RequestMapping("/third-part-app/request/status")
    public CommonResponse checkThirdPartAppRequestStatus(
            @RequestHeader String personId) {

        return userRegisterLoginService.checkThirdPartAppRequestStatus(personId);
    }

    @RequestMapping("/third-part-app/request/approve")
    public CommonResponse checkThirdPartAppRequestApprove(
            @RequestHeader String personId,
            @RequestParam String appName) {

        return userRegisterLoginService.checkThirdPartAppRequestApprove(appName);
    }
}
