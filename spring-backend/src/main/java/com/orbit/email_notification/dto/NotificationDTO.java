package com.orbit.email_notification.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.orbit.enums.NotificationType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class NotificationDTO {

    private Long id;
    private String subject;

    @Lob
    private String body;

    @NotBlank(message = "Recipient is required.")
    private String recipient;

    @Enumerated(EnumType.STRING)
    private NotificationType type;
    private LocalDateTime createdAt;
    private boolean isHtml;
}
