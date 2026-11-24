package com.orbit.email_notification.services;

import com.orbit.email_notification.dto.NotificationDTO;

public interface NotificationService {
    void sendEmail(NotificationDTO notificationDTO);
}
