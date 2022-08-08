package com.cb.notificationservice.beans.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NotificationParameter {
    private String notificationParameterName;
    private String notificationParameterValue;
}
