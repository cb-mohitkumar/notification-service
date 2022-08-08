package com.cb.notificationservice.repos;

import com.cb.notificationservice.beans.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationRepo extends MongoRepository<Notification, String> {
}
