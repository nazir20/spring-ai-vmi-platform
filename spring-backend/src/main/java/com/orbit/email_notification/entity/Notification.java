package com.orbit.email_notification.entity;

import com.orbit.enums.NotificationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;

    @Lob
    private String body;

    @NotBlank(message = "Recipient is required.")
    private String recipient;

    @Enumerated(EnumType.STRING)
    private NotificationType type;
    private final LocalDateTime createdAt = LocalDateTime.now();
    private boolean isHtml;
}
