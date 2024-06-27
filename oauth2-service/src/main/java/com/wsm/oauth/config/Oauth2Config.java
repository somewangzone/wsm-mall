package com.wsm.oauth.config;

import com.wsm.oauth.service.UserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;

// 第二步.需要实现验证服务配置类
@Configuration
@EnableAuthorizationServer
public class Oauth2Config extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserDetailServiceImpl userDetailService;

    @Autowired
    private AuthenticationManager authenticationManager;

    // 1.oauth2是为了生成 token 令牌的，token 令牌需要存储到哪里呢？所以需要先解决存储问题
    @Bean
    public TokenStore tokenStore() {
        return new JdbcTokenStore(dataSource);
    }

    // 2.关心一下client details表中的内容：client_id 和 client_secret. 从哪里获取？
    @Bean
    public ClientDetailsService clientDetails() {
        return new JdbcClientDetailsService(dataSource);
    }

    // 3.用户名和密码，以及 client_secret 是不是不能明文存储？是不是需要加密？
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 如果公司有自己的加密算法，可以通过这种形式进行 Encoder
        /*return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return null;
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return false;
            }
        }*/
        return new BCryptPasswordEncoder();
    }

    // 4.token 是不是得有过期时间？咋弄？
    // oauth2的默认过期时间是12h，如果想自定义它的过期时间，需要用到DefaultTokenService,并进行set
    @Primary
    @Bean
    public DefaultTokenServices defaultTokenServices() {
        DefaultTokenServices services = new DefaultTokenServices();
        services.setAccessTokenValiditySeconds(30 * 24 * 3600); // 30天过期
        services.setTokenStore(tokenStore()); // 用到第一步的tokenStore
        return services;
    }

    // 5.需要通过 clientDetailsServiceConfigurer 将我们的 clientDetailsService 设置到 token 中
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(clientDetails());
    }

    // 6.添加自定义的安全配置
    // 往往我们会将这个配置用于：放开一些接口的查询权限，比如说 checkToken 接口
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.allowFormAuthenticationForClients() // 可以进行表达提交
                .checkTokenAccess("permitAll()"); // 放开 checkToken
    }

    // 7. 第一步创建的 UserDetailsService 得安排一下
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.userDetailsService(userDetailService);
        endpoints.tokenServices(defaultTokenServices());
        endpoints.authenticationManager(this.authenticationManager);
        //endpoints.tokenStore(tokenStore());
    }
}
