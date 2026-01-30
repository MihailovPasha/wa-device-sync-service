package org.wa.device.sync.service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wa.device.sync.service.service.SyncService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService service;

    @PostMapping("/full-data")
    public Mono<Void> syncFullData(@RequestParam String email) {
        return service.syncFullData(email);
    }

    @PostMapping("/activity")
    public Mono<Void> syncActiveData(@RequestParam String email) {
        return service.syncActivityData(email);
    }

    @PostMapping("/sleep")
    public Mono<Void> syncSleepData(@RequestParam String email) {
        return service.syncSleepData(email);
    }

    @PostMapping("/heart-rate")
    public Mono<Void> syncHeartRateData(@RequestParam String email) {
        return service.syncHeartRateData(email);
    }

}
