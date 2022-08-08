package com.cb.notificationservice.beans.dtos;

import com.cb.notificationservice.beans.Notification;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationSendRequest {
    @NotBlank
    private List<String> customerIds;
    private String customerEmail;
    private String notificationTemplateName;
    //TODO: bad way of reusing class -> refactor
    private List<NotificationParameter> notificationParameters;
}
