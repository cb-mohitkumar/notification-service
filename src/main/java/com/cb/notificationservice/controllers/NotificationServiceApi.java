package com.cb.notificationservice.controllers;

import com.cb.notificationservice.beans.dtos.NotificationSendRequest;
import com.cb.notificationservice.beans.dtos.NotificationTemplateResponse;
import com.cb.notificationservice.services.NotificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/notifications")
public class NotificationServiceApi {
    private final NotificationService notificationService;
    final Logger logger = LoggerFactory.getLogger(NotificationServiceApi.class);

    @PostMapping
    public ResponseEntity<Object> send(@RequestBody NotificationSendRequest request) {
        logger.info("Sending notification: {} to customerId: {}", request.getNotificationTemplateName(), request.getCustomerIds().get(0));
        return ResponseEntity.ok(notificationService.sendNotification(request));
    }
}
