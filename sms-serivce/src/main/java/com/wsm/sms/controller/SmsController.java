package com.wsm.sms.controller;

import com.wsm.sms.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sms")
public class SmsController {

    @Autowired
    private SmsService smsService;

    @GetMapping("/send-msg-code")
    public void sendSms(@RequestParam(name = "phoneNumber")String phoneNumber){
        smsService.sendSms(phoneNumber);
    }
}
