package org.wa.device.sync.service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.wa.device.sync.service.dto.UserDto;
import org.wa.device.sync.service.exception.AuthServiceException;
import reactor.core.publisher.Mono;
import java.util.Map;

@Component
@Slf4j
public class AuthServiceClient {

    private final WebClient webClient;

    public AuthServiceClient(@Qualifier("authServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<UserDto> getUser(String email) {
        log.info("Получение пользователя: {}", email);

        return webClient.get()
                .uri("/{email}", email)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(error ->
                                Mono.error(new AuthServiceException("Не удалось получить пользователя"))
                        ))
                .bodyToMono(UserDto.class)
                .doOnSuccess(user ->
                        log.debug("Пользователь {} получен", user))
                .doOnError(error ->
                        log.error("Ошибка при запросе пользователя: ", error));
    }

    public Mono<Map<Long, UserDto>> getAllUsers() {
        log.info("Получение всех пользователей");

        return webClient.get()
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(error ->
                                Mono.error(new AuthServiceException(
                                        "Не удалось получить список пользователей. Статус: ",
                                        clientResponse.statusCode())
                                )
                        ))
                .bodyToMono(new ParameterizedTypeReference<Map<Long, UserDto>>() {})
                .doOnSuccess(users ->
                        log.info("Получено {} пользователей", users != null ? users.size() : 0))
                .doOnError(error ->
                        log.error("Ошибка при запросе пользователей: ", error));
    }
}