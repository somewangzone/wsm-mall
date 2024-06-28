package com.wsm.user.service;

import com.alibaba.fastjson.JSONObject;
import com.wsm.common.response.CommonResponse;
import com.wsm.common.response.ResponseCode;
import com.wsm.common.response.ResponseUtils;
import com.wsm.user.config.GiteeConfig;
import com.wsm.user.pojo.*;
import com.wsm.user.processor.RedisCommonProcessor;
import com.wsm.user.repo.OauthClientRegisterRepository;
import com.wsm.user.repo.OauthClientRepository;
import com.wsm.user.repo.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserRegisterLoginService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OauthClientRepository oauthClientRepository;

    @Autowired
    private RedisCommonProcessor redisCommonProcessor;

    @Autowired
    private RestTemplate innerRestTemplate;

    @Autowired
    private RestTemplate outerRestTemplate;

    @Resource(name = "transactionManager")
    private JpaTransactionManager transactionManager;

    @Autowired
    private GiteeConfig giteeConfig;

    @Autowired
    private OauthClientRegisterRepository oauthClientRegisterRepository;

    // 如果当前存在事务，就加入该事务，不存在就新建事务
    //@Transactional(propagation = Propagation.REQUIRED)
    public CommonResponse namePasswordRegister(User user) {
        // 新用户的注册
        if (null == userRepository.findByUserName(user.getUserName())
                && null == oauthClientRepository.findByClientId(user.getUserName())) {
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            String rawPasswd = user.getPasswd();
            String encodePassword = bCryptPasswordEncoder.encode(rawPasswd);
            user.setPasswd(encodePassword);// 报错加密的密码

            Oauth2Client oauth2Client = Oauth2Client.builder()
                    .clientId(user.getUserName())
                    .clientSecret(encodePassword)
                    .resourceIds(RegisterType.USER_PASSWORD.name())
                    .authorizedGrantTypes(AuthGrantType.refresh_token.name().concat(",").concat(AuthGrantType.password.name()))
                    .scope("web")
                    .authorities(RegisterType.USER_PASSWORD.name())
                    .build();

            //start 事务
            Integer uid = this.saveUerAndOauthClient(user, oauth2Client);
            //end 事务

            String personId = String.valueOf(uid + 10000000);
            redisCommonProcessor.setExpiredDays(personId, user, 30);

            // return user信息 + token 信息给前端
            // 查询语句，执行这个代码的时候，事务还没有commit，而我们的DB用的是 rr 模式，读取不到未 commit 的新数据
            // 因此需要把事务控制在save的时候
            Map oauth2ClientMap = generateOauthToken(AuthGrantType.password, user.getUserName(), rawPasswd,
                    oauth2Client.getClientId(), rawPasswd);

            return ResponseUtils.okResponse(formatResponseContent(user,
                    oauth2ClientMap));
            // start 事务结束，进行commit
        }
        return ResponseUtils.failResponse(ResponseCode.BAD_REQUEST.getCode(), null, "User already exist! Please Login");
        // start 事务结束，进行commit
    }

    public CommonResponse phoneCodeRegister(String phoneNumber, String code) {
        String cacheCode = String.valueOf(redisCommonProcessor.get(phoneNumber));
        if (StringUtils.isEmpty(cacheCode)) {
            return ResponseUtils.failResponse(ResponseCode.BAD_REQUEST.getCode(), null, "Phone code is expired!");
        }
        if (!cacheCode.equalsIgnoreCase(code)) {
            return ResponseUtils.failResponse(ResponseCode.BAD_REQUEST.getCode(), null, "Phone code is wrong!");
        }
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encodePassword = bCryptPasswordEncoder.encode(code);
        User user = userRepository.findByUserPhone(phoneNumber);
        if (null == user) {
            String userName = getSystemDefinedUserName(phoneNumber);
            user = User.builder()
                    .userName(userName)
                    .passwd(encodePassword)
                    .userPhone(phoneNumber)
                    .userRole(RegisterType.PHONE_NUMBER.name())
                    .build();

            Oauth2Client oauth2Client = Oauth2Client.builder()
                    .clientId(phoneNumber)
                    .clientSecret(encodePassword)
                    .resourceIds(RegisterType.PHONE_NUMBER.name())
                    .authorizedGrantTypes(AuthGrantType.refresh_token.name().concat(",")
                            .concat(AuthGrantType.client_credentials.name()))
                    .scope("web")
                    .authorities(RegisterType.PHONE_NUMBER.name())
                    .build();

            Integer uid = this.saveUerAndOauthClient(user, oauth2Client);
            String personId = String.valueOf(uid + 10000000);
            redisCommonProcessor.setExpiredDays(personId, user, 30);
        } else {
            // 有可能 token 已经过期了，需要更新一下密码
            oauthClientRepository.updateSecretByClientId(encodePassword, phoneNumber);
        }
        Map oauth2ClientMap = generateOauthToken(AuthGrantType.client_credentials, null, null,
                phoneNumber, code);

        return ResponseUtils.okResponse(formatResponseContent(user,
                oauth2ClientMap));
    }

    //https://gitee.com/oauth/authorize/?client_id=bfe27ac5249590de60a0781ba8ef97a2bb5b336e7f25d1c3629e65a6a684c7d7&redirect_uri=http://localhost:9001/user/register/gitee&response_type=code&state=GITEE
    public CommonResponse thirdPartGiteeCallback(HttpServletRequest request) {
        String code = request.getParameter("code");
        String state = request.getParameter("state");
        /*if(!giteeConfig.getState().equals(state)){
            throw new UnsupportedOperationException("Invalid state!");
        }*/
        String tokenUrl = String.format(giteeConfig.getTokenUrl(),
                giteeConfig.getClientId(), giteeConfig.getClientSecret(),
                giteeConfig.getCallBack(), code);

        JSONObject tokenObject = outerRestTemplate.postForObject(tokenUrl, null, JSONObject.class);
        String token = String.valueOf(tokenObject.get("access_token"));

        String userUrl = String.format(giteeConfig.getUserUrl(), token);
        JSONObject userObject = outerRestTemplate.getForObject(userUrl, JSONObject.class);

        String userName = giteeConfig.getState().concat(String.valueOf(userObject.get("name")));

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encodePassword = bCryptPasswordEncoder.encode(userName);

        User user = userRepository.findByUserName(userName);
        if (null == user) {
            user = User.builder()
                    .userName(userName)
                    .passwd(encodePassword)
                    .userRole(RegisterType.THIRD_PARTY.name())
                    .build();

            Oauth2Client oauth2Client = Oauth2Client.builder()
                    .clientId(userName)
                    .clientSecret(encodePassword)
                    .resourceIds(RegisterType.THIRD_PARTY.name())
                    .authorizedGrantTypes(AuthGrantType.refresh_token.name().concat(",")
                            .concat(AuthGrantType.client_credentials.name()))
                    .scope("web")
                    .authorities(RegisterType.THIRD_PARTY.name())
                    .build();

            Integer uid = this.saveUerAndOauthClient(user, oauth2Client);
            String personId = String.valueOf(uid + 10000000);
            redisCommonProcessor.setExpiredDays(personId, user, 30);
        }

        Map oauth2ClientMap = generateOauthToken(AuthGrantType.client_credentials, null, null,
                userName, userName);

        return ResponseUtils.okResponse(formatResponseContent(user,
                oauth2ClientMap));
    }

    private String getSystemDefinedUserName(String phoneNumber) {
        // 前缀 MALL_+当前时间+手机号后四位
        return "MALL_" + System.currentTimeMillis() + phoneNumber.substring(phoneNumber.length() - 4);
    }

    private Map formatResponseContent(User user, Map oauth2Client) {
        return new HashMap() {{
            put("user", user);
            put("oauth", oauth2Client);
        }};
    }

    private Integer saveUerAndOauthClient(User user, Oauth2Client oauth2Client) {
        // 手动的控制事务
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setTimeout(30);
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            user = this.userRepository.save(user);
            this.oauthClientRepository.save(oauth2Client);
            transactionManager.commit(status);
        } catch (Exception e) {
            if (!status.isCompleted()) {
                transactionManager.rollback(status);
            }
            throw new UnsupportedOperationException("DB Save failed!");
        }

        return user.getId();
    }

    private Map generateOauthToken(AuthGrantType authGrantType, String username, String password,
                                   String clientId, String clientSecret) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", authGrantType.name());
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        // 用户名+密码形式
        if (authGrantType == AuthGrantType.password) {
            params.add("username", username);
            params.add("password", password);
        }

        HttpEntity<MultiValueMap<String, String>> httpEntity =
                new HttpEntity<>(params, headers);

        return innerRestTemplate.postForObject("http://oauth2-service/oauth/token", httpEntity, Map.class);
    }

    public CommonResponse login(String userName, String password) {
        User user = userRepository.findByUserName(userName);
        if (null == user) {
            return ResponseUtils.failResponse(ResponseCode.BAD_REQUEST.getCode(), null, "User not exist!");
        }
        Map content = formatResponseContent(user,
                generateOauthToken(AuthGrantType.password, userName, password, userName, password));

        String personId = user.getId() + "1000000";
        redisCommonProcessor.setExpiredDays(personId, user, 30);
        return ResponseUtils.okResponse(content);
    }

    public CommonResponse thirdPartAppRequest(String personId, Ouath2ClientRegister ouath2ClientRegister) {
        Integer userId = Integer.valueOf(personId) - 1000000;
        ouath2ClientRegister.setClientId(UUID.randomUUID().toString().replaceAll("-", ""));
        ouath2ClientRegister.setClientSecret(UUID.randomUUID().toString().replaceAll("-", ""));
        ouath2ClientRegister.setAppLogo(0);
        ouath2ClientRegister.setUserId(userId);

        this.oauthClientRegisterRepository.save(ouath2ClientRegister);

        return ResponseUtils.okResponse(null);
    }

    public CommonResponse checkThirdPartAppRequestStatus(String personId) {
        Integer userId = Integer.valueOf(personId) - 1000000;
        List<Ouath2ClientRegister> userRequestInfo = this.oauthClientRegisterRepository.findByUserId(userId);

        return ResponseUtils.okResponse(userRequestInfo);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CommonResponse checkThirdPartAppRequestApprove(String appName) {
        Ouath2ClientRegister ouath2ClientRegister = this.oauthClientRegisterRepository.findByAppName(appName);

        this.oauthClientRegisterRepository.updateRegisterClientByAppName(appName);
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encodeClientSecret = bCryptPasswordEncoder.encode(ouath2ClientRegister.getClientSecret());

        Oauth2Client oauth2Client = Oauth2Client.builder()
                .clientId(ouath2ClientRegister.getClientId())
                .clientSecret(encodeClientSecret)
                .resourceIds(appName)
                .scope("web")
                .redirectUrl(ouath2ClientRegister.getAppCallbackUrl())
                .authorities(appName)
                .autoApprove("true")
                .authorizedGrantTypes(AuthGrantType.refresh_token.name().concat(",").concat(AuthGrantType.authorization_code.name()))
                .build();
        this.oauthClientRepository.save(oauth2Client);

        return ResponseUtils.okResponse(null);
    }
}
