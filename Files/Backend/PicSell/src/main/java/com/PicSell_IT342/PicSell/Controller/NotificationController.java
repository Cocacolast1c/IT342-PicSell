package com.PicSell_IT342.PicSell.Controller;

import com.PicSell_IT342.PicSell.Model.NotificationModel;
import com.PicSell_IT342.PicSell.Service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public List<NotificationModel> getAllNotifications() {
        return notificationService.getAllNotifications();
    }

    @GetMapping("/user/{userId}")
    public List<NotificationModel> getNotificationsByUserId(@PathVariable Long userId) {
        return notificationService.getNotificationsByUserId(userId);
    }

    @PostMapping
    public NotificationModel createNotification(@RequestBody NotificationModel notification) {
        return notificationService.saveNotification(notification);
    }

    @PutMapping("/user/{userId}/{notificationId}/read-status")
    public NotificationModel updateNotificationReadStatus(@PathVariable Long userId, @PathVariable Long notificationId) {
        return notificationService.updateNotificationReadStatus(userId, notificationId, true);
    }

    @DeleteMapping("/user/{userId}/{notificationId}")
    public void deleteNotification(@PathVariable Long userId, @PathVariable Long notificationId) {
        notificationService.deleteNotification(userId, notificationId);
    }
}