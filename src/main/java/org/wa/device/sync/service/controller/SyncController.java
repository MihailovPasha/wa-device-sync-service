package org.wa.device.sync.service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wa.device.sync.service.service.SyncService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/internal/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService service;

    @PostMapping("/full-data")
    public Mono<Void> syncFullData() {
        return service.syncFullData();
    }

    @PostMapping("/activity")
    public Mono<Void> syncActiveData() {
        return service.syncActivityData();
    }

    @PostMapping("/sleep")
    public Mono<Void> syncSleepData() {
        return service.syncSleepData();
    }

    @PostMapping("/heart-rate")
    public Mono<Void> syncHeartRateData() {
        return service.syncHeartRateData();
    }

}