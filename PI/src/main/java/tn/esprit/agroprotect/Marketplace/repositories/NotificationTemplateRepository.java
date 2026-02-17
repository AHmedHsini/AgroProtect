package com.example.agroprotect.repositories;

import com.example.agroprotect.entities.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    Optional<NotificationTemplate> findByCode(String code);

    Optional<NotificationTemplate> findByCodeAndLanguage(String code, String language);
}