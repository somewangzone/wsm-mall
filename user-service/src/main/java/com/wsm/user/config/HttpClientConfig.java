package com.wsm.user.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class HttpClientConfig {
    // 定义connection 超时时间
    // 定义 request 超时时间
    // 定义 socket 连接超时时间

    private static final int CONNECTION_TIMEOUT = 30000;
    private static final int REQUEST_TIMEOUT = 30000;
    private static final int SOCKET_TIMEOUT = 3000;

    // 连接池的配置，pooling的管理
    private static final int MAX_TOTAL_CONNECTIONS = 50;
    // 默认的连接池中的 keep-alive 线程的配置，如果我们的header中携带了超时时间，则优先使用header中的
    private static final int DEFAULT_KEEP_ALIVE_TIME_MS = 20000;
    // 清理我们的空闲线程的一个定时任务
    private static final int CLOSE_IDLE_CONNECTION_WAIT_TIME_SEC = 30;

    @Bean
    public CloseableHttpClient httpClient() {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(CONNECTION_TIMEOUT)
                .setConnectionRequestTimeout(REQUEST_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .build();

        return HttpClients.custom()
                .setDefaultRequestConfig(config)
                .setConnectionManager(connectionManager()) // 线程池pooling
                .setKeepAliveStrategy(connectionKeepAliveStrategy()) // 空闲策略
                .build();
    }

    // 连接池
    @Bean
    public PoolingHttpClientConnectionManager connectionManager() {
        // 其实这个东西就是为了加入我们的 max_total_connections
        // 除此之外，还要进行https 和 http 注册

        // https
        SSLContextBuilder builder = new SSLContextBuilder();
        try {
            // 如果有key store (trustStore)
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());// 内部系统采用自签名
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            log.error("loadTrustMaterial failed,error details = {}", e);
        }

        SSLConnectionSocketFactory sslcsf = null;
        try {
            sslcsf = new SSLConnectionSocketFactory(builder.build());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("SSLConnectionSocketFactory create failed,error details = {}", e);
        }

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                .<ConnectionSocketFactory>create()
                .register("https", sslcsf)
                .register("http", new PlainConnectionSocketFactory())
                .build();

        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager =
                new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        poolingHttpClientConnectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);

        return poolingHttpClientConnectionManager;
    }

    @Bean
    public ConnectionKeepAliveStrategy connectionKeepAliveStrategy() {
        ConnectionKeepAliveStrategy connectionKeepAliveStrategy = (httpResponse, httpContext) -> {
            HeaderElementIterator it =
                    new BasicHeaderElementIterator(httpResponse.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                HeaderElement headerElement = it.nextElement();
                String name = headerElement.getName();
                String value = headerElement.getValue();
                if (null != value && name.equalsIgnoreCase("timeout")) {
                    return Long.parseLong(value) * 1000;
                }
            }
            return DEFAULT_KEEP_ALIVE_TIME_MS;
        };
        return connectionKeepAliveStrategy;
    }

    // 定期清理空闲的connection + 过期的 connection
    // 根源来源于 PoolingHttpClientConnectionManager
    @Bean
    public Runnable idleConnectionMonitor(final PoolingHttpClientConnectionManager connectionManager) {
        return new Runnable() {
            @Override
            @Scheduled(fixedDelay = 10000)
            public void run() {
                try {
                    if (null != connectionManager) {
                        connectionManager.closeExpiredConnections();
                        connectionManager.closeIdleConnections(CLOSE_IDLE_CONNECTION_WAIT_TIME_SEC, TimeUnit.SECONDS);
                    } else {
                        log.warn("PoolingHttpClientConnectionManager not init!");
                    }
                } catch (Exception e) {
                    log.error("close connections error, details={}", e);
                }
            }
        };
    }
}
