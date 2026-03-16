package org.wa.device.sync.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.wa.device.sync.service.dto.health.HealthRawData;

@Mapper(componentModel = "spring")
public interface FullDataMapper {

    @Mapping(target = "externalId", source = "activityData.externalId")
    @Mapping(target = "source", source = "activityData.source")
    @Mapping(target = "timestamp", source = "activityData.timestamp")
    @Mapping(target = "steps", source = "activityData.steps")
    @Mapping(target = "heartRate", source = "heartRateData.heartRate")
    @Mapping(target = "sleepHours", source = "sleepData.sleepHours")
    HealthRawData combine(HealthRawData activityData, HealthRawData heartRateData, HealthRawData sleepData);
}
