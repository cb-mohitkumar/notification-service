package com.cb.notificationservice.beans.dtos;

import com.cb.notificationservice.beans.NotificationTemplate;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class NotificationTemplateResponse {
    private List<NotificationTemplate> notificationTemplates;
    private String status;
    private String statusDescription;
}
