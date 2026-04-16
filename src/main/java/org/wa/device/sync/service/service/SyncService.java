package org.wa.device.sync.service.service;

import reactor.core.publisher.Mono;

public interface SyncService {

    Mono<Void> syncFullData();

    Mono<Void> syncActivityData();

    Mono<Void> syncSleepData();

    Mono<Void> syncHeartRateData();
}