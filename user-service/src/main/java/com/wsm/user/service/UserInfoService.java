package com.wsm.user.service;

import com.wsm.common.response.CommonResponse;
import com.wsm.common.response.ResponseCode;
import com.wsm.common.response.ResponseUtils;
import com.wsm.user.pojo.User;
import com.wsm.user.processor.RedisCommonProcessor;
import com.wsm.user.repo.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserInfoService {

    @Autowired
    private RedisCommonProcessor redisCommonProcessor;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate innerRestTemplate;

    public CommonResponse checkPhoneBindStatus(String personId) {
        User user = (User) redisCommonProcessor.get(personId);
        boolean isBind = false;
        if (null != user) {
            isBind = user.getUserPhone() != null;
            return ResponseUtils.okResponse(isBind);
        }

        Integer userId = Integer.valueOf(personId) - 100000;
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            isBind = userOptional.get().getUserPhone() != null;
            redisCommonProcessor.setExpiredDays(personId, userOptional.get(), 30);
            return ResponseUtils.okResponse(isBind);
        }

        return ResponseUtils.failResponse(ResponseCode.BAD_REQUEST.getCode(), null, "Invalid user!");
    }

    public CommonResponse bindPhoneNumber(String personId, String phoneNumber, String code) {
        // 采用延时双删的策略，需要在finally中实现第二次删的逻辑，避免阻塞正常的调用
        /*try {
            // 更新 DB
            String cacheCode = String.valueOf(redisCommonProcessor.get(phoneNumber));
            if (StringUtils.isEmpty(cacheCode)) {
                return ResponseUtils.failResponse(ResponseCode.BAD_REQUEST.getCode(), null, "Phone code is expired!");
            }
            if (!cacheCode.equalsIgnoreCase(code)) {
                return ResponseUtils.failResponse(ResponseCode.BAD_REQUEST.getCode(), null, "Phone code is wrong!");
            }

            int userId = Integer.valueOf(personId) - 1000000;
            Optional<User> userOptional = userRepository.findById(userId);
            if (!userOptional.isPresent()) {
                return ResponseUtils.failResponse(ResponseCode.BAD_REQUEST.getCode(), null, "Invalid user!");
            }

            userRepository.updatePhoneById(phoneNumber, userId);
            // 更新了数据库需要同步更新redis
            // 删除redis中的数据，可以采用延时双删的策略（但是存在网络抖动，这里可以失败），
            // 这里采用了canal，来实现双写一致性
            redisCommonProcessor.remove(personId);

            return ResponseUtils.okResponse(null);
        } catch (Exception e) {
            return ResponseUtils.failResponse(ResponseCode.BAD_REQUEST.getCode(), null, "Server error!");
        } finally {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            redisCommonProcessor.remove(personId);
        }*/

        // 这里采用 canal 来实现 双写一致性问题
        String cacheCode = String.valueOf(redisCommonProcessor.get(phoneNumber));
        if (StringUtils.isEmpty(cacheCode)) {
            return ResponseUtils.failResponse(ResponseCode.BAD_REQUEST.getCode(), null, "Phone code is expired!");
        }
        if (!cacheCode.equalsIgnoreCase(code)) {
            return ResponseUtils.failResponse(ResponseCode.BAD_REQUEST.getCode(), null, "Phone code is wrong!");
        }

        int userId = Integer.valueOf(personId) - 1000000;
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            return ResponseUtils.failResponse(ResponseCode.BAD_REQUEST.getCode(), null, "Invalid user!");
        }

        userRepository.updatePhoneById(phoneNumber, userId);
        // 这里先删除一次（避免网络问题，这里可以不实现这个删除，canal有可能比这个删除更快），再采用了canal，来实现双写一致性
        redisCommonProcessor.remove(personId);

        return ResponseUtils.okResponse(null);
    }

    public CommonResponse getUserInfoByToken(String token) {
        Map results = innerRestTemplate.getForObject("http://oauth2-service/oauth/check-token?token=" + token,
                Map.class);

        boolean active = Boolean.valueOf(String.valueOf(results.get("active")));
        if (!active) {
            return ResponseUtils.failResponse(ResponseCode.BAD_REQUEST.getCode(), null, "token is not active");
        }
        String userName = String.valueOf(results.get("user_name"));

        return ResponseUtils.okResponse(new HashMap() {{
            put("userName", userName);
        }});
    }
}
