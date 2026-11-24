package com.orbit.alert.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.orbit.enums.AlertSeverity;
import com.orbit.enums.AlertStatus;
import com.orbit.enums.AlertTriggerSource;
import com.orbit.enums.AlertType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
public class AlertDTO {

    private Long id;

    @NotNull(message = "Alert type is required.")
    private AlertType alertType;

    @NotNull(message = "Alert severity is required.")
    private AlertSeverity severity;

    @NotBlank(message = "Alert title is required.")
    private String title;

    @NotBlank(message = "Alert message is required.")
    private String message;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private AlertStatus status;

    private Long siloId;

    private Long shipmentId;

    private Long purchaseOrderId;

    @NotNull(message = "Alert trigger source is required.")
    private AlertTriggerSource triggerSource;

    private Long triggerReferenceId;

    @NotNull(message = "Alert trigger context is required.")
    private Map<String, Object> triggerContext;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant createdAt;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant updatedAt;
}
