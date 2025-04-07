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
    public String createNotification(@RequestBody NotificationModel notification) {
        try {
            if (notification.getUser() == null || notification.getMessage() == null) {
                return "User or Message cannot be null";
            }
            if (notification.getUser().getUserId() == null) {
                return "User ID cannot be null";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
        notificationService.saveNotification(notification);
        return "Notification created successfully";
    }

    @PutMapping("/user/{userId}/{notificationId}/read-status")
    public String updateNotificationReadStatus(@PathVariable Long userId, @PathVariable Long notificationId) {
        try {
            if (userId == null || notificationId == null) {
                return "User ID or Notification ID cannot be null";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
        notificationService.updateNotificationReadStatus(userId, notificationId, true);
        return "Notification read status updated successfully";
    }

    @DeleteMapping("/user/{userId}/{notificationId}")
    public String deleteNotification(@PathVariable Long userId, @PathVariable Long notificationId) {
        try {
            if (userId == null || notificationId == null) {
                return "User ID or Notification ID cannot be null";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
        notificationService.deleteNotification(userId, notificationId);
        return "Notification deleted successfully";
    }
}