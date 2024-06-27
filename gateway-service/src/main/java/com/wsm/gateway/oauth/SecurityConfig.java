//package com.wsm.gateway.oauth;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.authentication.ReactiveAuthenticationManager;
//import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
//import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
//import org.springframework.security.config.web.server.ServerHttpSecurity;
//import org.springframework.security.oauth2.server.resource.web.server.ServerBearerTokenAuthenticationConverter;
//import org.springframework.security.web.server.SecurityWebFilterChain;
//import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
//
//import javax.sql.DataSource;
//
//// 第二步：
//@Configuration
//@EnableWebFluxSecurity
//public class SecurityConfig {
//
//    @Autowired
//    private DataSource dataSource;
//
//    @Autowired
//    private AccessManager accessManager;
//
//    @Bean
//    public SecurityWebFilterChain webFluxSecurityWebFilterChain(ServerHttpSecurity serverHttpSecurity) {
//
//        // 1.DB交互类的创建，把 dataSource 融入代码中
//        ReactiveAuthenticationManager reactiveAuthenticationManager
//                = new ReactiveJdbcAuthenticationManager(dataSource);
//
//        AuthenticationWebFilter filter = new AuthenticationWebFilter(reactiveAuthenticationManager);
//
//        filter.setServerAuthenticationConverter(new ServerBearerTokenAuthenticationConverter());
//
//        serverHttpSecurity.httpBasic().disable()
//                .csrf().disable()
//                .authorizeExchange()
//                .pathMatchers(HttpMethod.OPTIONS).permitAll()
//                .anyExchange().access(accessManager)
//                .and()
//                .addFilterAt(filter, SecurityWebFiltersOrder.AUTHENTICATION);
//
//        return serverHttpSecurity.build();
//    }
//
//}
