package com.gestion.gestion_bus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertDTO {
    private String id;
    private String severity;
    private String busId;
    private String message;
    private Instant timestamp;
    private String status;
}