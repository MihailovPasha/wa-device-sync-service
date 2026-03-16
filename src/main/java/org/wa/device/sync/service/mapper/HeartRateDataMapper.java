package org.wa.device.sync.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.wa.device.sync.service.dto.health.HealthRawData;
import org.wa.device.sync.service.dto.health.HeartRateDataResponse;

@Mapper(componentModel = "spring")
public interface HeartRateDataMapper {

    String DEFAULT_SOURCE = "google";

    @Mapping(target = "externalId", source = "email")
    @Mapping(target = "timestamp", source = "date")
    @Mapping(target = "heartRate", source = "averageBpm")
    @Mapping(target = "steps", ignore = true)
    @Mapping(target = "sleepHours", ignore = true)
    @Mapping(target = "source", constant = DEFAULT_SOURCE)
    HealthRawData toHealthRawData(HeartRateDataResponse response);
}
