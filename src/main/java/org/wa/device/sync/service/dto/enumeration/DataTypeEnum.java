package org.wa.device.sync.service.dto.enumeration;

import lombok.Getter;

@Getter
public enum DataTypeEnum {
    FULL_HEALTH("здоровья"),
    ACTIVITY("активности"),
    HEART_RATE("сердцебиения"),
    SLEEP("сна");

    private final String description;

    DataTypeEnum(final String description) {
        this.description = description;
    }
}
