package org.wa.device.sync.service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.wa.device.sync.service.client.AuthServiceClient;
import org.wa.device.sync.service.client.GoogleIntegrationClient;
import org.wa.device.sync.service.dto.UserDto;
import org.wa.device.sync.service.dto.health.HealthRawData;
import org.wa.device.sync.service.service.SyncService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SyncServiceImpl implements SyncService {

    @Value("${spring.kafka.template.default-topic}")
    private String healthRawGoogleTopic;
    private final GoogleIntegrationClient googleIntegrationClient;
    private final AuthServiceClient authServiceClient;
    private final KafkaTemplate<String, HealthRawData> kafkaTemplate;

    @Override
    public Mono<Void> syncFullData(String email) {
        return sendData(email, "здоровья",
                () -> googleIntegrationClient.fetchFullHealthData(email));
    }

    @Override
    public Mono<Void> syncActivityData(String email) {
        return sendData(email, "активности",
                () -> googleIntegrationClient.fetchActivityData(email));
    }

    @Override
    public Mono<Void> syncSleepData(String email) {
        return sendData(email, "сна",
                () -> googleIntegrationClient.fetchSleepData(email));
    }

    @Override
    public Mono<Void> syncHeartRateData(String email) {
        return sendData(email, "сердцебиения",
                () -> googleIntegrationClient.fetchHeartRateData(email));
    }

    @Override
    @Scheduled(cron = "0 0 2 * * ?")
    public Mono<Void> performDailyFullSync() {
        log.info("Запуск ежедневной полной синхронизации (02:00)");

        return authServiceClient.getAllUsers()
                .flatMap(usersMap -> {
                    Map<Long, UserDto> users = usersMap.entrySet().stream()
                            .filter(entry -> entry.getValue().getGoogleRefreshToken() != null &&
                                    !entry.getValue().getGoogleRefreshToken().isEmpty())
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    Map.Entry::getValue
                            ));

                    log.info("Найдено {} пользователей с Google Refresh Token", users.size());

                    if (users.isEmpty()) {
                        log.warn("Нет пользователей с Google Refresh Token для синхронизации");
                        return Mono.empty();
                    }

                    return Flux.fromIterable(users.entrySet())
                            .flatMap(entry -> {
                                UserDto user = entry.getValue();

                                return syncUserHealthData(user)
                                        .doOnSuccess(v -> log.debug("Успешно синхронизирован пользователь: {}",
                                                user.getEmail()))
                                        .doOnError(e -> log.error("Ошибка синхронизации для {}: {}",
                                                user.getEmail(), e.getMessage()))
                                        .onErrorResume(e -> Mono.empty());
                            }, 5)
                            .then()
                            .doOnSuccess(v -> log.info("Ежедневная синхронизация завершена"));
                })
                .doOnError(e ->
                        log.error("Критическая ошибка в ежедневной синхронизации: {}", e.getMessage()));
    }

    private Mono<Void> sendData(String email, String dataType, Supplier<Mono<HealthRawData>> dataFetcher) {
        return dataFetcher.get()
                .flatMap(healthData ->
                        Mono.fromCompletionStage(
                                kafkaTemplate.send(healthRawGoogleTopic, email, healthData)
                        )
                )
                .doOnSuccess(sendResult ->
                        log.info("Отправлены данные {} пользователя: {}", dataType, email))
                .doOnError(ex ->
                        log.error("Ошибка отправления данных {} для пользователя {}: ", dataType, email, ex))
                .then();
    }

    private Mono<Void> syncUserHealthData(UserDto userDto) {
        return googleIntegrationClient.fetchFullHealthData(userDto.getEmail())
                .flatMap(healthData ->
                        Mono.fromCompletionStage(
                                kafkaTemplate.send(healthRawGoogleTopic, userDto.getEmail(), healthData)
                        )
                )
                .then();
    }
}
