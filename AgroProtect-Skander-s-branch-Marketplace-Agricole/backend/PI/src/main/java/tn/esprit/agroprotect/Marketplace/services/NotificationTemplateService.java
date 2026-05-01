package  tn.esprit.agroprotect.Marketplace.services;

import  tn.esprit.agroprotect.Marketplace.entities.NotificationTemplate;

import java.util.List;

public interface NotificationTemplateService {

    NotificationTemplate createTemplate(NotificationTemplate template);

    NotificationTemplate updateTemplate(Long id, NotificationTemplate template);

    void deleteTemplate(Long id);

    NotificationTemplate getTemplateById(Long id);

    List<NotificationTemplate> getAllTemplates();

    NotificationTemplate getTemplateByCode(String code);


}