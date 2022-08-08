package com.cb.notificationservice.services;

import com.cb.notificationservice.beans.CbConfig;
import com.cb.notificationservice.beans.CbConfig.GmailGatewayConfig;
import com.cb.notificationservice.beans.NotificationPreference;
import com.cb.notificationservice.beans.NotificationTemplate;
import com.cb.notificationservice.beans.dtos.NotificationSendRequest;
import com.cb.notificationservice.beans.dtos.NotificationSendResponse;
import com.cb.notificationservice.beans.dtos.NotificationTemplateRequest;
import com.cb.notificationservice.beans.dtos.NotificationTemplateResponse;
import com.cb.notificationservice.components.NotificationTemplateServiceIntegrator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.cb.notificationservice.beans.CbConfig.*;
import static com.cb.notificationservice.beans.enums.NotificationType.EMAIL;
import static com.cb.notificationservice.beans.enums.NotificationType.SMS;

@Slf4j
@Service
@AllArgsConstructor
public class NotificationService {
    private final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender emailSender;
    private final CbConfig mailConfigs;
    private final NotificationTemplateServiceIntegrator templateServiceIntegrator;
    private final RestTemplate restTemplate;


    public NotificationSendResponse sendNotification(NotificationSendRequest request) {
        NotificationSendResponse response = createNotificationSendSuccessResponse();
        // Get notification preferences for each customer

        // Get email and/or phone number of each customer
        NotificationPreference notificationPreference = findNotificationPreferencesForRecipient(request.getCustomerIds().get(0));
        List<NotificationTemplate> templates = fetchNotificationTemplates(request);
        // Based on preferences send the notifications
        if (notificationPreference.getPreferredNotificationTypes().contains(EMAIL)) {
            Optional<NotificationTemplate> templateOptional = templates.stream().filter(p -> "EMAIL".equalsIgnoreCase(p.getTemplateType())).findFirst();
            templateOptional.ifPresent(
                    template -> {
                        GmailGatewayConfig gmailGatewayConfig = mailConfigs.getGmailGatewayConfig();
                        if(Objects.nonNull(request.getCustomerEmail())) gmailGatewayConfig.setCustomerEmail(request.getCustomerEmail());
                        sendEmailNotification(templateOptional.get(), gmailGatewayConfig, response);
                    });
        }
        if (notificationPreference.getPreferredNotificationTypes().contains(SMS)) {
            Optional<NotificationTemplate> templateOptional = templates.stream().filter(p -> "SMS".equalsIgnoreCase(p.getTemplateType())).findFirst();
            templateOptional.ifPresent(
                    template -> sendSMSNotification(templateOptional.get(), mailConfigs.getTwilioSMSGatewayConfig(), response));
        }
        return response;
    }

    private List<NotificationTemplate> fetchNotificationTemplates(NotificationSendRequest request) {
        NotificationTemplateRequest templateRequest = new NotificationTemplateRequest();
        templateRequest.setCustomerId(request.getCustomerIds().get(0));
        templateRequest.setNotificationParameters(request.getNotificationParameters());
        templateRequest.setNotificationTemplateName(request.getNotificationTemplateName());
        NotificationTemplateResponse templateResponse = templateServiceIntegrator.getNotificationTemplate(templateRequest);
        return templateResponse.getNotificationTemplates();
    }

    private void sendSMSNotification(NotificationTemplate request, TwilioSmsGatewayConfiguration smsConfig, NotificationSendResponse response) {
        Twilio.init(smsConfig.getTwilioUserName(), smsConfig.getTwilioPassword());
        Message message = Message.creator(
                        new com.twilio.type.PhoneNumber(smsConfig.getCustomerMobile()),
                        new com.twilio.type.PhoneNumber(smsConfig.getSmsFrom()),
                        request.getSmsContent())
                .create();
        logger.info("Message sent successfully. Message Id: {}", message.getSid());
    }

    private void sendEmailNotification(NotificationTemplate request, GmailGatewayConfig mailConfigs, NotificationSendResponse response) {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());
            helper.addAttachment("Chargebee.png", new ClassPathResource("Chargebee.png"));
            helper.setTo(mailConfigs.getCustomerEmail());
            helper.setFrom(mailConfigs.getSenderEmail());
            helper.setText(request.getEmailContent(), true);
            helper.setSubject(request.getEmailSubject());
            emailSender.send(message);
        } catch (MessagingException e) {
            logger.error("Failed to send notification with exception: {}", e.getStackTrace());
            response.setStatus("FAILURE");
            response.setStatusDescription("Notification Failed");
        }
    }

    public NotificationPreference findNotificationPreferencesForRecipient(String recipientId) {
        // Notification Preference service
        RestTemplate restTemplate = new RestTemplate();
        String fooResourceUrl = "http://localhost:9092/notification-preferences/";
        ResponseEntity<NotificationPreference> response
                = restTemplate.getForEntity(fooResourceUrl + recipientId, NotificationPreference.class);

        return response.getBody();
    }

    private NotificationSendResponse createNotificationSendSuccessResponse() {
        NotificationSendResponse response = new NotificationSendResponse();
        response.setStatus("SUCCESS");
        response.setStatusDescription("Notification Received Successfully");
        return response;
    }
}
