package org.wa.device.sync.service.config;

import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import org.wa.device.sync.service.exception.ConnectException;
import reactor.netty.http.client.HttpClient;
import java.time.Duration;

@Configuration
@Slf4j
public class WebClientConfig {

    @Value("${google-fit-integration.base-url}")
    private String googleBaseUrl;
    @Value("${auth-service.base-url}")
    private String authServiceBaseUrl;
    @Value("${google-fit-integration.timeout}")
    private int timeout;

    @Bean("googleIntegrationWebClient")
    public WebClient googleIntegrationWebClient() {
        log.info("Создание WebClient для Google Fit Integration Service: {}", googleBaseUrl);

        return getWebClient(googleBaseUrl);
    }

    @Bean("authServiceWebClient")
    public WebClient authServiceWebClient() {
        log.info("Создание WebClient для Auth Service: {}", authServiceBaseUrl);

        return getWebClient(authServiceBaseUrl);
    }

    private WebClient getWebClient(final String baseUrl) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(timeout)).
                option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout);

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .filter(ExchangeFilterFunctions.statusError(
                        HttpStatusCode::isError,
                        response -> new ConnectException(
                                "Ошибка соединения с сервисом: " + response.statusCode())
                ))
                .build();
    }
}
