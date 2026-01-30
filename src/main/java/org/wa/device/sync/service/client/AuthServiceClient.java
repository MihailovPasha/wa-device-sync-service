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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AuthServiceClient {

    private final WebClient webClient;

    public AuthServiceClient(@Qualifier("authServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<Map<Long, UserDto>> getAllUsers() {
        log.info("Получение всех пользователей");
        return getAllUsersAsList()
                .map(users -> users.stream()
                        .collect(Collectors.toMap(
                                UserDto::getId,
                                Function.identity(),
                                (existing, replacement) -> existing
                        )))
                .doOnSuccess(map ->
                        log.info("Количество пользователей = {}", map.size()))
                .doOnError(e ->
                        log.error("Ошибка преобразования пользователей в Map ", e));
    }

    public Mono<UserDto> getUser(String email) {
        return webClient.get()
                .uri("/email/{email}", email)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(error ->
                                Mono.error(new AuthServiceException(
                                        String.format("Не удалось получить пользователя: " + email))
                                )
                        ))
                .bodyToMono(UserDto.class)
                .doOnSuccess(user ->
                        log.info("Пользователь {} получен", user))
                .doOnError(error ->
                        log.error("Ошибка при запросе пользователя: ", error));
    }

    private Mono<List<UserDto>> getAllUsersAsList() {
        return webClient.get()
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(error ->
                                Mono.error(new AuthServiceException(
                                        "Не удалось получить список пользователей. Статус: ",
                                        clientResponse.statusCode())
                                )
                        ))
                .bodyToMono(new ParameterizedTypeReference<List<UserDto>>() {})
                .doOnSuccess(users ->
                        log.info("Получено {} пользователей", users != null ? users.size() : 0))
                .doOnError(error ->
                        log.error("Ошибка при запросе пользователей: ", error));
    }
}