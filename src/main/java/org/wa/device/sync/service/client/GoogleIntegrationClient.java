package org.wa.device.sync.service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.wa.device.sync.service.dto.health.HealthRawData;
import org.wa.device.sync.service.exception.AuthServiceException;
import org.wa.device.sync.service.exception.GoogleIntegrationException;
import org.wa.device.sync.service.dto.health.ActivityDataResponse;
import org.wa.device.sync.service.dto.health.HeartRateDataResponse;
import org.wa.device.sync.service.dto.health.SleepDataResponse;
import org.wa.device.sync.service.mapper.ActivityDataMapper;
import org.wa.device.sync.service.mapper.HeartRateDataMapper;
import org.wa.device.sync.service.mapper.SleepDataMapper;
import reactor.core.publisher.Mono;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.function.Function;

@Component
@Slf4j
public class GoogleIntegrationClient {

    private final WebClient webClient;
    private final AuthServiceClient authClient;
    private final ActivityDataMapper activityDataMapper;
    private final HeartRateDataMapper heartRateDataMapper;
    private final SleepDataMapper sleepDataMapper;

    public GoogleIntegrationClient(ActivityDataMapper activityDataMapper,
                                   HeartRateDataMapper heartRateDataMapper,
                                   SleepDataMapper sleepDataMapper,
                                   AuthServiceClient authClient,
                                   @Qualifier("googleIntegrationWebClient") WebClient webClient) {
        this.activityDataMapper = activityDataMapper;
        this.heartRateDataMapper = heartRateDataMapper;
        this.sleepDataMapper = sleepDataMapper;
        this.authClient = authClient;
        this.webClient = webClient;
    }

    public Mono<HealthRawData> fetchActivityData(String email) {
        return getAuthUser(email)
                .flatMap(refreshToken ->
                        fetchData(email, refreshToken, OffsetDateTime.now(ZoneOffset.UTC),
                                "активности", "/activity",
                                ActivityDataResponse.class, activityDataMapper::toHealthRawData)
                );
    }

    public Mono<HealthRawData> fetchHeartRateData(String email) {
        return getAuthUser(email)
                .flatMap(refreshToken ->
                        fetchData(email, refreshToken, OffsetDateTime.now(ZoneOffset.UTC),
                                "сердцебиения", "/heart-rate",
                                HeartRateDataResponse.class, heartRateDataMapper::toHealthRawData)
                );
    }

    public Mono<HealthRawData> fetchSleepData(String email) {
        return getAuthUser(email)
                .flatMap(refreshToken ->
                        fetchData(email, refreshToken, OffsetDateTime.now(ZoneOffset.UTC),
                                "сна", "/sleep",
                                SleepDataResponse.class, sleepDataMapper::toHealthRawData)
                );
    }

    public Mono<HealthRawData> fetchFullHealthData(String email) {
        log.info("Получение данных здоровья...");
        return fetchActivityData(email)
                .flatMap(activityData -> fetchHeartRateData(email)
                        .flatMap(heartRateData -> fetchSleepData(email)
                                .map(sleepData -> combineHealthData(
                                        activityData,
                                        heartRateData,
                                        sleepData
                                ))
                        )
                );
    }

    private <T> Mono<HealthRawData> fetchData(String email, String refreshToken, OffsetDateTime date,
                                              String dataType, String path,
                                              Class<T> responseType,
                                              Function<T, HealthRawData> mapper) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return Mono.error(new GoogleIntegrationException(
                    "Refresh token пустой для пользователя: " + email));
        }

        log.info("Получение данных {} пользователя: {}", dataType, email);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .queryParam("email", email)
                        .queryParam("refreshToken", refreshToken)
                        .queryParam("date", date)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(error ->
                                Mono.error(new GoogleIntegrationException(
                                        String.format("Не удалось получить данные %s для пользователя: %s",
                                                dataType, email))
                                )
                        )
                )
                .bodyToMono(responseType)
                .map(mapper);
    }

    private HealthRawData combineHealthData(
            HealthRawData activityData,
            HealthRawData heartRateData,
            HealthRawData sleepData) {

        return HealthRawData.builder()
                .userId(activityData.getUserId())
                .source(activityData.getSource())
                .timestamp(activityData.getTimestamp())
                .steps(activityData.getSteps())
                .heartRate(heartRateData.getHeartRate())
                .sleepHours(sleepData.getSleepHours())
                .build();
    }

    private Mono<String> getAuthUser(String email) {
        return authClient.getUser(email)
                .flatMap(user -> {
                    if (user != null &&
                            user.getGoogleRefreshToken() != null &&
                            !user.getGoogleRefreshToken().isEmpty()) {
                        return Mono.just(user.getGoogleRefreshToken());
                    } else {
                        return Mono.error(new GoogleIntegrationException(
                                "Google Refresh Token не найден для пользователя: " + email));
                    }
                })
                .onErrorResume(AuthServiceException.class, e -> {
                    log.warn("Пользователь {} не найден в Auth Service: {}", email, e.getMessage());
                    return Mono.error(new AuthServiceException(
                            "Пользователь не найден: " + email));
                })
                .onErrorResume(e -> {
                    log.error("Ошибка получения пользователя {}: {}", email, e.getMessage());
                    return Mono.error(new AuthServiceException(
                            "Ошибка получения refresh token для пользователя: " + email));
                });
    }
}
