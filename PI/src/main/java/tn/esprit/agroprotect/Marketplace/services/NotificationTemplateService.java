package com.example.agroprotect.services;

import com.example.agroprotect.entities.NotificationTemplate;

import java.util.List;

public interface NotificationTemplateService {

    NotificationTemplate createTemplate(NotificationTemplate template);

    NotificationTemplate updateTemplate(Long id, NotificationTemplate template);

    void deleteTemplate(Long id);

    NotificationTemplate getTemplateById(Long id);

    List<NotificationTemplate> getAllTemplates();

    NotificationTemplate getTemplateByCode(String code);

    NotificationTemplate getTemplateByCodeAndLanguage(String code, String language);
}