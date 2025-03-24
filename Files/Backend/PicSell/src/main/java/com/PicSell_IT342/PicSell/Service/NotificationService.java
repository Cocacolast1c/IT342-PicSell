package com.PicSell_IT342.PicSell.Service;

import com.PicSell_IT342.PicSell.Model.NotificationModel;
import com.PicSell_IT342.PicSell.Repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    public List<NotificationModel> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public List<NotificationModel> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserUserId(userId);
    }

    public NotificationModel saveNotification(NotificationModel notification) {
        return notificationRepository.save(notification);
    }

    public NotificationModel updateNotificationReadStatus(Long userId, Long notificationId, boolean isRead) {
        Optional<NotificationModel> optionalNotification = notificationRepository.findById(notificationId);
        if (optionalNotification.isPresent()) {
            NotificationModel notification = optionalNotification.get();
            if (notification.getUser().getUserId().equals(userId)) {
                notification.setRead(isRead);
                return notificationRepository.save(notification);
            }
        }
        return null;
    }

    public void deleteNotification(Long userId, Long notificationId) {
        Optional<NotificationModel> optionalNotification = notificationRepository.findById(notificationId);
        if (optionalNotification.isPresent()) {
            NotificationModel notification = optionalNotification.get();
            if (notification.getUser().getUserId().equals(userId)) {
                notificationRepository.delete(notification);
            }
        }
    }
}