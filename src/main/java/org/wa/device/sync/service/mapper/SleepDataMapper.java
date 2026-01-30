package org.wa.device.sync.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.wa.device.sync.service.dto.health.HealthRawData;
import org.wa.device.sync.service.dto.health.SleepDataResponse;

@Mapper(componentModel = "spring")
public interface SleepDataMapper {

    String DEFAULT_SOURCE = "google";

    @Mapping(target = "userId", source = "email")
    @Mapping(target = "timestamp", source = "date")
    @Mapping(target = "sleepHours", source = "totalSleepHours")
    @Mapping(target = "heartRate", ignore = true)
    @Mapping(target = "steps", ignore = true)
    @Mapping(target = "source", constant = DEFAULT_SOURCE)
    HealthRawData toHealthRawData(SleepDataResponse response);
}
