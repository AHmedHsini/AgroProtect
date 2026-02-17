package com.example.agroprotect.repositories;

import com.example.agroprotect.entities.NotificationHistory;
import com.example.agroprotect.entities.StatusNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {

    List<NotificationHistory> findByTo(Long to);

    List<NotificationHistory> findByFrom(Long from);

    List<NotificationHistory> findByStatus(StatusNotification status);

    List<NotificationHistory> findByToAndStatus(Long to, StatusNotification status);

    List<NotificationHistory> findByTemplateId(Long templateId);
}