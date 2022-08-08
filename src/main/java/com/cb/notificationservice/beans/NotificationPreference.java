package com.cb.notificationservice.beans;

import com.cb.notificationservice.beans.enums.NotificationType;
import lombok.Data;

import java.util.List;

@Data
public class NotificationPreference {
    private String customerId;
    private List<NotificationType> preferredNotificationTypes;
}
