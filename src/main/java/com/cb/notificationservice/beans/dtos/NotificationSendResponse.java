package com.cb.notificationservice.beans.dtos;

import lombok.Data;

@Data
public class NotificationSendResponse {
    private String status;
    private String statusDescription;
}
