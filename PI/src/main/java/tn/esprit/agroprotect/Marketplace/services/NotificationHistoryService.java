package com.example.agroprotect.services;

import com.example.agroprotect.entities.NotificationHistory;
import com.example.agroprotect.entities.StatusNotification;

import java.util.List;

public interface NotificationHistoryService {

    NotificationHistory createNotification(NotificationHistory notification);

    NotificationHistory getNotificationById(Long id);

    List<NotificationHistory> getAllNotifications();

    List<NotificationHistory> getNotificationsByRecipient(Long to);

    List<NotificationHistory> getNotificationsBySender(Long from);

    List<NotificationHistory> getNotificationsByStatus(StatusNotification status);

    NotificationHistory updateStatus(Long id, StatusNotification status);
}