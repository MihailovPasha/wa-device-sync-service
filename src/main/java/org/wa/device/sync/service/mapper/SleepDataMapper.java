package org.wa.device.sync.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.wa.device.sync.service.dto.health.HealthRawData;
import org.wa.device.sync.service.dto.health.SleepDataResponse;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface SleepDataMapper {

    String DEFAULT_SOURCE = "google";

    @Mapping(target = "externalId", source = "externalId")
    @Mapping(target = "timestamp", source = "response.date")
    @Mapping(target = "sleepHours", source = "response.totalSleepHours")
    @Mapping(target = "heartRate", ignore = true)
    @Mapping(target = "steps", ignore = true)
    @Mapping(target = "source", constant = DEFAULT_SOURCE)
    HealthRawData toHealthRawData(SleepDataResponse response, UUID externalId);
}
