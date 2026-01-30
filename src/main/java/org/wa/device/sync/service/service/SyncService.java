package org.wa.device.sync.service.service;

import reactor.core.publisher.Mono;

public interface SyncService {

    Mono<Void> syncFullData(String email);

    Mono<Void> syncActivityData(String email);

    Mono<Void> syncSleepData(String email);

    Mono<Void> syncHeartRateData(String email);

    Mono<Void> performDailyFullSync();
}
