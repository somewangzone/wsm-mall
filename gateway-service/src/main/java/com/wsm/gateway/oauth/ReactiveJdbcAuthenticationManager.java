//package com.wsm.gateway.oauth;
//
//import org.springframework.security.authentication.ReactiveAuthenticationManager;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.common.OAuth2AccessToken;
//import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
//import org.springframework.security.oauth2.provider.OAuth2Authentication;
//import org.springframework.security.oauth2.provider.token.TokenStore;
//import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
//import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
//import reactor.core.publisher.Mono;
//
//import javax.sql.DataSource;
//
//// 第三步：完成和数据库的交互
//public class ReactiveJdbcAuthenticationManager implements ReactiveAuthenticationManager {
//
//    private TokenStore tokenStore;
//
//    public ReactiveJdbcAuthenticationManager(DataSource dataSource) {
//        this.tokenStore = new JdbcTokenStore(dataSource);
//    }
//
//    @Override
//    public Mono<Authentication> authenticate(Authentication authentication) {
//        return Mono.justOrEmpty(authentication)
//                .filter(a -> a instanceof BearerTokenAuthenticationToken) // 后续会通过将我们的token，放到http header authentication 中进行访问，并携带bearer前缀
//                .cast(BearerTokenAuthenticationToken.class)
//                .map(BearerTokenAuthenticationToken::getToken) // 获取到token
//                .flatMap((accessToken) -> {
//                    // token 从数据库查询
//                    OAuth2AccessToken oAuth2AccessToken = this.tokenStore.readAccessToken(accessToken);
//                    if (null == oAuth2AccessToken) {
//                        return Mono.error(new InvalidTokenException("InvalidToken"));
//                    } else if (oAuth2AccessToken.isExpired()) {
//                        return Mono.error(new InvalidTokenException("InvalidToken,isExpired!"));
//                    }
//                    // token 存在于DB，并且没有过期，难道一定是oAuth2的token吗？如果一个黑客在DB中插入了一个token 123，并设置永不过期
//                    OAuth2Authentication oAuth2Authentication = this.tokenStore.readAuthentication(accessToken);
//                    if (null == oAuth2Authentication) {
//                        return Mono.error(new InvalidTokenException("Fake Token"));
//                    }
//
//                    return Mono.justOrEmpty(oAuth2Authentication);
//                }).cast(Authentication.class);
//    }
//}
