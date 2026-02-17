package com.example.agroprotect.services;

import com.example.agroprotect.entities.NotificationHistory;
import com.example.agroprotect.entities.StatusNotification;
import com.example.agroprotect.repositories.NotificationHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationHistoryServiceImpl implements NotificationHistoryService {

    @Autowired
    private NotificationHistoryRepository historyRepository;

    @Override
    public NotificationHistory createNotification(NotificationHistory notification) {
        return historyRepository.save(notification);
    }

    @Override
    public NotificationHistory getNotificationById(Long id) {
        return historyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
    }

    @Override
    public List<NotificationHistory> getAllNotifications() {
        return historyRepository.findAll();
    }

    @Override
    public List<NotificationHistory> getNotificationsByRecipient(Long to) {
        return historyRepository.findByTo(to);
    }

    @Override
    public List<NotificationHistory> getNotificationsBySender(Long from) {
        return historyRepository.findByFrom(from);
    }

    @Override
    public List<NotificationHistory> getNotificationsByStatus(StatusNotification status) {
        return historyRepository.findByStatus(status);
    }

    @Override
    public NotificationHistory updateStatus(Long id, StatusNotification status) {
        NotificationHistory notification = getNotificationById(id);
        notification.setStatus(status);
        return historyRepository.save(notification);
    }
}