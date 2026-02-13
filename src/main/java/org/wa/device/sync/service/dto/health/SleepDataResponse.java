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
public class SleepDataResponse {
    private String email;
    private double totalSleepHours;
    private OffsetDateTime date;
}
