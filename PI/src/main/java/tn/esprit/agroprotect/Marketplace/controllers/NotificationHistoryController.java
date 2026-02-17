package com.example.agroprotect.controllers;

import com.example.agroprotect.entities.NotificationHistory;
import com.example.agroprotect.entities.StatusNotification;
import com.example.agroprotect.services.NotificationHistoryService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping({"/NotificationHistory"})
public class NotificationHistoryController {

    NotificationHistoryService historyService;

    @PostMapping("/addNotification")
    public NotificationHistory addNotification(@RequestBody NotificationHistory notification) {
        return historyService.createNotification(notification);
    }

    @GetMapping("/getAll")
    public List<NotificationHistory> getAllNotification() {
        return historyService.getAllNotifications();
    }

    @GetMapping("/getById/{id}")
    public NotificationHistory getById(@PathVariable Long id) {
        return historyService.getNotificationById(id);
    }

    @GetMapping("/getByRecipient/{to}")
    public List<NotificationHistory> getByRecipient(@PathVariable Long to) {
        return historyService.getNotificationsByRecipient(to);
    }

    @GetMapping("/getBySender/{from}")
    public List<NotificationHistory> getBySender(@PathVariable Long from) {
        return historyService.getNotificationsBySender(from);
    }

    @GetMapping("/getByStatus/{status}")
    public List<NotificationHistory> getByStatus(@PathVariable StatusNotification status) {
        return historyService.getNotificationsByStatus(status);
    }

    @PutMapping("/updateStatus/{id}/{status}")
    public NotificationHistory updateStatus(@PathVariable Long id, @PathVariable StatusNotification status) {
        return historyService.updateStatus(id, status);
    }
}