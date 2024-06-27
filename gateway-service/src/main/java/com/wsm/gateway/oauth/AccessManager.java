//package com.wsm.gateway.oauth;
//
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.security.authorization.AuthorizationDecision;
//import org.springframework.security.authorization.ReactiveAuthorizationManager;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.provider.OAuth2Authentication;
//import org.springframework.security.web.server.authorization.AuthorizationContext;
//import org.springframework.stereotype.Component;
//import org.springframework.util.AntPathMatcher;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//import java.util.Set;
//import java.util.concurrent.ConcurrentSkipListSet;
//
//// 第一步：对一些不需要进行token校验的，需要进行token校验的做处理
//@Component
//public class AccessManager implements ReactiveAuthorizationManager<AuthorizationContext> {
//
//    // 有一些路径不需要token校验(正则表达式)
//    private Set<String> permitAll = new ConcurrentSkipListSet<>();
//
//    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();
//
//    public AccessManager() {
//        // 对于获取 token 的路径，比如 http://localhost:8500/oauth/token，在获取token 的路上，本身就没有token，必须放行
//        permitAll.add("/**/auth/**");
//    }
//
//    // 决定是否放行的最终函数！！！ webFlux Mono
//    @Override
//    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext context) {
//        // exchange 中包含我们的 request 信息，能够获取访问路径，只有获取到访问路径后才能判断是否放行，
//        // 如果不放行，就得 DB 交互,进行真正的校验
//        ServerWebExchange exchange = context.getExchange();
//
//        return authentication.map(auth -> {
//            // 获取 path
//            String path = exchange.getRequest().getURI().getPath();
//            if (checkPermit(path)) {
//                return new AuthorizationDecision(true); // 放行
//            }
//            // auth 是 OAuth 类型的
//            if (auth instanceof OAuth2Authentication) {
//                OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) auth;
//                String clientId = oAuth2Authentication.getOAuth2Request().getClientId();
//                if (StringUtils.isNotEmpty(clientId)) {
//                    return new AuthorizationDecision(true);
//                }
//            }
//            return new AuthorizationDecision(false);
//        });
//    }
//
//    private boolean checkPermit(String path) {
//        return permitAll.stream().filter(p -> antPathMatcher.match(p, path)).findFirst().isPresent();
//    }
//}
