package org.wa.device.sync.service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.wa.device.sync.service.dto.PaginatedUserDto;
import org.wa.device.sync.service.dto.UserDto;
import org.wa.device.sync.service.exception.AuthException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Objects;

@Component
@Slf4j
public class AuthServiceClient {

    private final WebClient webClient;

    public AuthServiceClient(@Qualifier("authServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<UserDto> getAllUsers() {
        log.info("Получение всех пользователей");

        return webClient.get()
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(error ->
                                Mono.error(new AuthException(
                                        "Не удалось получить пользователей. Статус: ",
                                        clientResponse.statusCode())
                                )
                        ))
                .bodyToFlux(PaginatedUserDto.class)
                .doOnNext(page ->
                        log.debug("Получена страница {}/{} с {} пользователями",
                                page.getCurrentPage() + 1,
                                page.getTotalPages(),
                                page.getUsers().size()))
                .flatMap(page -> Flux.fromIterable(page.getUsers()))
                .filter(Objects::nonNull)
                .doOnNext(user -> log.debug("Обработан пользователь: {}", user.getEmail()))
                .doOnComplete(() -> log.info("Потоковая передача пользователей завершена"))
                .doOnError(e -> log.error("Ошибка потоковой передачи", e));
    }
}