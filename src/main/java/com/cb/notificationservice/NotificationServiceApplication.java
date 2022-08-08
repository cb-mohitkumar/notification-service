package com.cb.notificationservice;

import com.cb.notificationservice.beans.CbConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.client.RestTemplate;

import java.util.Properties;

@SpringBootApplication
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

    @Bean
    public CbConfig mailConfigs() {
        // Notification Preference service
        RestTemplate restTemplate = new RestTemplate();
        String resourceUrl
                = "http://localhost:9092/configs/type/COMMUNICATION";
        ResponseEntity<CbConfig> response
                = restTemplate.getForEntity(resourceUrl, CbConfig.class);

        return response.getBody();
    }

    @Bean
    public RestTemplate templateServiceClient() {
        return new RestTemplateBuilder()
                .defaultHeader("Content-Type", "application/json")
                .rootUri("http://localhost:9093/notification-templates/template")
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public JavaMailSender javaMailSender() {
        CbConfig cbConfig = mailConfigs();
        CbConfig.GmailGatewayConfig mailConfig = cbConfig.getGmailGatewayConfig();
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setProtocol(mailConfig.getMailProtocol());
        javaMailSender.setHost(mailConfig.getMailHost());
        javaMailSender.setPort(Integer.parseInt(mailConfig.getMailPort()));
        javaMailSender.setUsername(mailConfig.getSenderEmail());
        javaMailSender.setPassword(System.getenv("GMAIL_PASSWORD"));
        Properties props = javaMailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", mailConfig.isMailSmtpAuth());
        props.put("mail.smtp.starttls.enable", mailConfig.isSmtpStartTLS());
        props.put("mail.debug", "true");
        return javaMailSender;
    }
}

