package org.wa.device.sync.service.dto.health;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HeartRateDataResponse {
    private String email;
    private double averageBpm;
    private double restingBpm;
    private List<Double> bpmSeries;
    private OffsetDateTime date;
}
