package org.wa.device.sync.service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.wa.device.sync.service.dto.UserDto;
import org.wa.device.sync.service.dto.enumeration.DataTypeEnum;
import org.wa.device.sync.service.dto.health.ActivityDataResponse;
import org.wa.device.sync.service.dto.health.HealthRawData;
import org.wa.device.sync.service.dto.health.HeartRateDataResponse;
import org.wa.device.sync.service.dto.health.SleepDataResponse;
import org.wa.device.sync.service.exception.GoogleIntegrationException;
import org.wa.device.sync.service.mapper.ActivityDataMapper;
import org.wa.device.sync.service.mapper.FullDataMapper;
import org.wa.device.sync.service.mapper.HeartRateDataMapper;
import org.wa.device.sync.service.mapper.SleepDataMapper;
import reactor.core.publisher.Mono;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.function.Function;

@Component
@Slf4j
public class GoogleScheduledClient {

    private final WebClient webClient;
    private final ActivityDataMapper activityDataMapper;
    private final HeartRateDataMapper heartRateDataMapper;
    private final SleepDataMapper sleepDataMapper;
    private final FullDataMapper fullDataMapper;

    public GoogleScheduledClient(ActivityDataMapper activityDataMapper,
                                 HeartRateDataMapper heartRateDataMapper,
                                 SleepDataMapper sleepDataMapper,
                                 FullDataMapper fullDataMapper,
                                 @Qualifier("googleIntegrationWebClient") WebClient webClient) {
        this.activityDataMapper = activityDataMapper;
        this.heartRateDataMapper = heartRateDataMapper;
        this.sleepDataMapper = sleepDataMapper;
        this.fullDataMapper = fullDataMapper;
        this.webClient = webClient;
    }

    public Mono<HealthRawData> fetchFullHealthData(UserDto user) {
        String email = user.getEmail();
        String refreshToken = user.getGoogleRefreshToken();

        if (refreshToken == null || refreshToken.isEmpty()) {
            return Mono.error(new GoogleIntegrationException(
                    "Отсутствует refresh token пользователя: " + email));
        }

        log.info("Получение данных здоровья {}", email);

        return Mono.zip(
                fetchActivityData(email, refreshToken),
                fetchHeartRateData(email, refreshToken),
                fetchSleepData(email, refreshToken)
        ).map(tuple -> fullDataMapper.combine(
                tuple.getT1(),
                tuple.getT2(),
                tuple.getT3()
        ));
    }

    private Mono<HealthRawData> fetchActivityData(String email, String refreshToken) {
        return fetchData(email, refreshToken, DataTypeEnum.ACTIVITY.getDescription(), "/activity",
                ActivityDataResponse.class, activityDataMapper::toHealthRawData);
    }

    private Mono<HealthRawData> fetchHeartRateData(String email, String refreshToken) {
        return fetchData(email, refreshToken, DataTypeEnum.HEART_RATE.getDescription(), "/heart-rate",
                HeartRateDataResponse.class, heartRateDataMapper::toHealthRawData);
    }

    private Mono<HealthRawData> fetchSleepData(String email, String refreshToken) {
        return fetchData(email, refreshToken, DataTypeEnum.SLEEP.getDescription(), "/sleep",
                SleepDataResponse.class, sleepDataMapper::toHealthRawData);
    }

    private <T> Mono<HealthRawData> fetchData(String email, String refreshToken,
                                              String dataType, String path,
                                              Class<T> responseType,
                                              Function<T, HealthRawData> mapper) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .queryParam("email", email)
                        .queryParam("refreshToken", refreshToken)
                        .queryParam("date", OffsetDateTime.now(ZoneOffset.UTC))
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
}
