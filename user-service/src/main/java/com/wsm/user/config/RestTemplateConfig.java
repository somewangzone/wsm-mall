package com.wsm.user.config;

import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

@Configuration
public class RestTemplateConfig {

    @Autowired
    private CloseableHttpClient httpClient;

    // restTemplate 的底层使用的是 jdk 的 url connection,没有什么过期时间的设置，对于一些复杂场景
    // 不太适用，所有打算替换掉底层的url connection，转而使用 apache 的 httpClient
    // 并且在 httpClient 中，配置我们的 连接超时，线程池大小，keep-alive配置，定时清理空闲线程的任务等
    @Bean(name="innerRestTemplate")
    @LoadBalanced
    public RestTemplate innerRestTemplate(){
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());

        // 我们一般接收的 utf-8 形式的消息
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        Iterator<HttpMessageConverter<?>> iterator = messageConverters.iterator();
        while(iterator.hasNext()){
            HttpMessageConverter<?> httpMessageConverter = iterator.next();
            if(httpMessageConverter instanceof StringHttpMessageConverter){
                ((StringHttpMessageConverter)httpMessageConverter).setDefaultCharset(Charset.forName("UTF-8"));
            }
        }

        return restTemplate;
    }

    // 调用外部的，没有使用LoadBalance
    @Bean(name="outerRestTemplate")
    public RestTemplate outerRestTemplate(){
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());

        // 我们一般接收的 utf-8 形式的消息
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        Iterator<HttpMessageConverter<?>> iterator = messageConverters.iterator();
        while(iterator.hasNext()){
            HttpMessageConverter<?> httpMessageConverter = iterator.next();
            if(httpMessageConverter instanceof StringHttpMessageConverter){
                ((StringHttpMessageConverter)httpMessageConverter).setDefaultCharset(Charset.forName("UTF-8"));
            }
        }

        return restTemplate;
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory(){
        HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory =
                new HttpComponentsClientHttpRequestFactory();
        httpComponentsClientHttpRequestFactory.setHttpClient(httpClient);

        return httpComponentsClientHttpRequestFactory;
    }
}
