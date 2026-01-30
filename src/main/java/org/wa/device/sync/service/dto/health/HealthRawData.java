package org.wa.device.sync.service.dto.health;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Builder
public class HealthRawData {
    private String userId;
    private String source;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssX", timezone = "UTC")
    private OffsetDateTime timestamp;
    private Double heartRate;
    private Integer steps;
    private Double sleepHours;
}
