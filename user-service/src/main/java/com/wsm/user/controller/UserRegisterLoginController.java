package com.wsm.user.controller;

import com.wsm.common.response.CommonResponse;
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

        return userRegisterLoginService.phoneCodeRegister(phoneNumber,code);
    }

    // 第三方授权，gitee
    // 这个接口是 第三方平台调用咱们的，叫回调接口
    @RequestMapping("/gitee")
    public CommonResponse thirdPartGiteeCallback(HttpServletRequest request) {

        return userRegisterLoginService.thirdPartGiteeCallback(request);
    }
}
