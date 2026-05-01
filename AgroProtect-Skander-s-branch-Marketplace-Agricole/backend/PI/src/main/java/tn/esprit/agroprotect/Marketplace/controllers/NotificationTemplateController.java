package  tn.esprit.agroprotect.Marketplace.controllers;

import  tn.esprit.agroprotect.Marketplace.entities.NotificationTemplate;
import  tn.esprit.agroprotect.Marketplace.services.NotificationTemplateService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
@RestController
@RequestMapping({"/NotificationTemplate"})
public class NotificationTemplateController {

    NotificationTemplateService templateService;

    @PostMapping("/addTemplate")
    public NotificationTemplate addTemplate(@RequestBody NotificationTemplate template) {
        return templateService.createTemplate(template);
    }

    @GetMapping("/getAll")
    public List<NotificationTemplate> getAllTemplate() {
        return templateService.getAllTemplates();
    }

    @GetMapping("/getById/{id}")
    public NotificationTemplate getById(@PathVariable Long id) {
        return templateService.getTemplateById(id);
    }

    @PutMapping("/updateTemplate/{id}")
    public NotificationTemplate updateTemplate(@PathVariable Long id, @RequestBody NotificationTemplate template) {
        return templateService.updateTemplate(id, template);
    }

    @DeleteMapping("/deleteTemplate/{id}")
    public void deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
    }

    @GetMapping("/getByCode/{code}")
    public NotificationTemplate getByCode(@PathVariable String code) {
        return templateService.getTemplateByCode(code);
    }

}