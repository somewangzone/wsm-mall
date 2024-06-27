package com.wsm.gateway.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(value = "oauth2-service")
public interface Oauth2ServiceClient {

    // http://localhost:8500/oauth/check_token?token=24f60abb-97c5-4063-9f4e-35104b725b32
    @GetMapping("/oauth/check_token")
    Map<String,Object> checkToken(@RequestParam(value = "token") String token);
}
