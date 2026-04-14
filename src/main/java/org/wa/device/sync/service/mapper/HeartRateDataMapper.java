package org.wa.device.sync.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.wa.device.sync.service.dto.health.HealthRawData;
import org.wa.device.sync.service.dto.health.HeartRateDataResponse;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface HeartRateDataMapper {

    String DEFAULT_SOURCE = "google";

    @Mapping(target = "externalId", source = "externalId")
    @Mapping(target = "timestamp", source = "response.date")
    @Mapping(target = "heartRate", source = "response.averageBpm")
    @Mapping(target = "steps", ignore = true)
    @Mapping(target = "sleepHours", ignore = true)
    @Mapping(target = "source", constant = DEFAULT_SOURCE)
    HealthRawData toHealthRawData(HeartRateDataResponse response, UUID externalId);
}
