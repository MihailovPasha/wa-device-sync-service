package org.wa.device.sync.service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.wa.device.sync.service.client.AuthServiceClient;
import org.wa.device.sync.service.client.GoogleIntegrationClient;
import org.wa.device.sync.service.client.GoogleScheduledClient;
import org.wa.device.sync.service.dto.UserDto;
import org.wa.device.sync.service.dto.enumeration.DataTypeEnum;
import org.wa.device.sync.service.dto.health.HealthRawData;
import org.wa.device.sync.service.service.SyncService;
import reactor.core.publisher.Mono;
import java.util.function.Supplier;

@Service
@Slf4j
@RequiredArgsConstructor
public class SyncServiceImpl implements SyncService {

    @Value("${spring.kafka.template.default-topic}")
    private String healthRawGoogleTopic;
    private final GoogleScheduledClient googleScheduledClient;
    private final GoogleIntegrationClient googleIntegrationClient;
    private final AuthServiceClient authServiceClient;
    private final KafkaTemplate<String, HealthRawData> kafkaTemplate;

    @Override
    public Mono<Void> syncFullData(String email) {
        return sendData(email, DataTypeEnum.FULL_HEALTH.getDescription(),
                () -> googleIntegrationClient.fetchFullHealthData(email));
    }

    @Override
    public Mono<Void> syncActivityData(String email) {
        return sendData(email, DataTypeEnum.ACTIVITY.getDescription(),
                () -> googleIntegrationClient.fetchActivityData(email));
    }

    @Override
    public Mono<Void> syncSleepData(String email) {
        return sendData(email, DataTypeEnum.SLEEP.getDescription(),
                () -> googleIntegrationClient.fetchSleepData(email));
    }

    @Override
    public Mono<Void> syncHeartRateData(String email) {
        return sendData(email, DataTypeEnum.HEART_RATE.getDescription(),
                () -> googleIntegrationClient.fetchHeartRateData(email));
    }

    @Scheduled(cron = "* * 2 * * ?")
    @SchedulerLock(
            name = "dailyFullSync",
            lockAtLeastFor = "5m",
            lockAtMostFor = "60m"
    )
    public Mono<Void> performDailyFullSync() {
        log.info("Запуск ежедневной полной синхронизации (02:00)");

        return authServiceClient.getAllUsers()
                .flatMap(user -> syncUserHealthData(user)
                        .onErrorResume(ex -> {
                            log.error("Ошибка для {}: {}", user.getEmail(), ex.getMessage());
                            return Mono.empty();
                        }), 5)
                .then()
                .doOnSuccess(mess -> log.info("Ежедневная синхронизация завершена"))
                .doOnError(ex -> log.error("Ошибка синхронизации: ", ex));
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
        return googleScheduledClient.fetchFullHealthData(userDto)
                .flatMap(healthData ->
                        Mono.fromCompletionStage(
                                kafkaTemplate.send(healthRawGoogleTopic, userDto.getEmail(), healthData)
                        )
                )
                .then();
    }
}