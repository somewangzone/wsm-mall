package com.wsm.gateway.filter;

import com.wsm.gateway.feignclient.Oauth2ServiceClient;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class AuthFilter implements GlobalFilter, Ordered {

    @Lazy // gateway是使用的webflux，内部采用的netty，使用rest形式会造成启动死锁，需要让行webflux的加载
    @Autowired
    private Oauth2ServiceClient oauth2ServiceClient;

    @SneakyThrows
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String path = request.getURI().getPath();
        if (path.contains("/oauth")
                || path.contains("/user/register")) {
            return chain.filter(exchange);
        }

        // 自定义 header 中需要有 Authorization，但不需要添加 bearer
        String token = request.getHeaders().getFirst("Authorization");

        //block()/blockFirst()/blockLast() are blocking, which is not supported in thread reactor-http-nio-3异常，需要异步
        CompletableFuture<Map> future = CompletableFuture.supplyAsync(() ->
                oauth2ServiceClient.checkToken(token));
        Map result = future.get();

        boolean active = (boolean) result.get("active");
        if (!active) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        // 给微服务转发请求的时候带上一些header
        ServerHttpRequest httpRequest = request.mutate().headers(httpHeaders -> {
            httpHeaders.set("personId", request.getHeaders().getFirst("personId"));
            // httpHeaders.set("tracingId", "123");
        }).build();

        exchange.mutate().request(httpRequest);

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
