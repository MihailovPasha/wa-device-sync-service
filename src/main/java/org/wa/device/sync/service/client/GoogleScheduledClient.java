package org.wa.device.sync.service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.UUID;
import java.util.function.Function;

@Component
@Slf4j
public class GoogleScheduledClient {

    private final WebClient webClient;
    private final ActivityDataMapper activityDataMapper;
    private final HeartRateDataMapper heartRateDataMapper;
    private final SleepDataMapper sleepDataMapper;
    private final FullDataMapper fullDataMapper;
    @Value("${google-fit-integration.endpoint.activity}")
    private String activityPath;
    @Value("${google-fit-integration.endpoint.heart-rate}")
    private String heartRatePath;
    @Value("${google-fit-integration.endpoint.sleep}")
    private String sleepPath;

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
        UUID externalId = user.getId();
        String email = user.getEmail();
        String refreshToken = user.getGoogleRefreshToken();

        if (refreshToken == null || refreshToken.isEmpty()) {
            return Mono.error(new GoogleIntegrationException(
                    "Отсутствует refresh token пользователя: " + email));
        }

        log.info("Получение данных здоровья {}", email);

        return Mono.zip(
                fetchActivityData(email, refreshToken, externalId),
                fetchHeartRateData(email, refreshToken, externalId),
                fetchSleepData(email, refreshToken, externalId)
        ).map(tuple -> fullDataMapper.combine(
                tuple.getT1(),
                tuple.getT2(),
                tuple.getT3()
        ));
    }

    private Mono<HealthRawData> fetchActivityData(String email, String refreshToken, UUID externalId) {
        return fetchData(email, refreshToken, DataTypeEnum.ACTIVITY.getDescription(), activityPath,
                ActivityDataResponse.class,
                response -> activityDataMapper.toHealthRawData(response, externalId));
    }

    private Mono<HealthRawData> fetchHeartRateData(String email, String refreshToken, UUID externalId) {
        return fetchData(email, refreshToken, DataTypeEnum.HEART_RATE.getDescription(), heartRatePath,
                HeartRateDataResponse.class,
                response -> heartRateDataMapper.toHealthRawData(response, externalId));
    }

    private Mono<HealthRawData> fetchSleepData(String email, String refreshToken, UUID externalId) {
        return fetchData(email, refreshToken, DataTypeEnum.SLEEP.getDescription(), sleepPath,
                SleepDataResponse.class,
                response -> sleepDataMapper.toHealthRawData(response, externalId));
    }

    private <T> Mono<HealthRawData> fetchData(String email, String refreshToken,
                                              String dataType, String path,
                                              Class<T> responseType,
                                              Function<T, HealthRawData> mapper) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .queryParam("email", email)
                        .queryParam("refreshToken", refreshToken)
                        .queryParam("date", OffsetDateTime.now(ZoneOffset.UTC).minusDays(1))
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
