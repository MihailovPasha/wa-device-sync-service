package org.wa.device.sync.service.dto.health;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityDataResponse {
    private String email;
    private OffsetDateTime date;
    private int steps;
    private double distanceKm;
    private double calories;
}
