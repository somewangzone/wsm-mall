package com.wsm.oauth.config;

import com.wsm.oauth.service.UserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// 第三步：Web 安全的配置
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailServiceImpl userDetailService;

    // 1.构造 Oauth2Config 中的注入的 AuthenticationManager
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    // 2.解决跨域问题 cors,csrf
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests().anyRequest().authenticated()
                .and().httpBasic()
                .and().cors()
                .and().csrf().disable(); // 为什么要disable csrf , spring security 引入了csrf，默认是开启的，csrf和rest中的post有冲突，应该禁用掉
    }

    // 3.所有的访问都需要 oauth 验证嚒，比如 api-doc、swagger-ui。放行点东西
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers("/swagger-*");
    }

    // 4.第1个的方法太拉跨了，直接用的super的authenticationManagerBean方法，有点low。
    // 我们高级着，我们需要控制super.authenticationManagerBean()方法中所有使用的AuthenticationManagerBuilder
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailService)
                .passwordEncoder(new PasswordEncoder() {
                    @Override
                    public String encode(CharSequence rawPassword) {
                        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                        return passwordEncoder.encode(rawPassword);
                    }

                    @Override
                    public boolean matches(CharSequence rawPassword, String encodedPassword) {
                        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                        return passwordEncoder.matches(rawPassword, encodedPassword);
                    }
                });
    }
}
