package org.wa.device.sync.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.wa.device.sync.service.dto.health.ActivityDataResponse;
import org.wa.device.sync.service.dto.health.HealthRawData;

@Mapper(componentModel = "spring")
public interface ActivityDataMapper {

    String DEFAULT_SOURCE = "google";

    @Mapping(target = "userId", source = "email")
    @Mapping(target = "timestamp", source = "date")
    @Mapping(target = "steps", source = "steps")
    @Mapping(target = "heartRate", ignore = true)
    @Mapping(target = "sleepHours", ignore = true)
    @Mapping(target = "source", constant = DEFAULT_SOURCE)
    HealthRawData toHealthRawData(ActivityDataResponse response);
}
