package  tn.esprit.agroprotect.Marketplace.services;

import  tn.esprit.agroprotect.Marketplace.entities.NotificationTemplate;
import  tn.esprit.agroprotect.Marketplace.repositories.NotificationTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationTemplateServiceImpl implements NotificationTemplateService {

    @Autowired
    private NotificationTemplateRepository templateRepository;

    @Override
    public NotificationTemplate createTemplate(NotificationTemplate template) {
        return templateRepository.save(template);
    }

    @Override
    public NotificationTemplate updateTemplate(Long id, NotificationTemplate template) {
        NotificationTemplate existingTemplate = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));

        existingTemplate.setCode(template.getCode());
        existingTemplate.setSubject(template.getSubject());
        existingTemplate.setBody(template.getBody());


        return templateRepository.save(existingTemplate);
    }

    @Override
    public void deleteTemplate(Long id) {
        templateRepository.deleteById(id);
    }

    @Override
    public NotificationTemplate getTemplateById(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));
    }

    @Override
    public List<NotificationTemplate> getAllTemplates() {
        return templateRepository.findAll();
    }

    @Override
    public NotificationTemplate getTemplateByCode(String code) {
        return templateRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Template not found"));
    }


}