package com.example.appointmentservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        // Configure connection pooling
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setDefaultMaxPerRoute(20);
        connManager.setMaxTotal(200);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connManager)
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        // set factory-level timeouts (milliseconds)
        factory.setConnectionRequestTimeout(5_000);
        factory.setReadTimeout(5_000);

        return new RestTemplate(factory);
        // // Configure timeouts for inter-service calls
        // RequestConfig requestConfig = RequestConfig.custom()
        //     .setConnectTimeout(5_000)
        //     .setConnectionRequestTimeout(5_000)
        //     .setSocketTimeout(5_000)
        //     .build();

        // CloseableHttpClient httpClient = HttpClientBuilder.create()
        //     .setDefaultRequestConfig(requestConfig)
        //     .build();

        // HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        // return new RestTemplate(factory);
    }
}