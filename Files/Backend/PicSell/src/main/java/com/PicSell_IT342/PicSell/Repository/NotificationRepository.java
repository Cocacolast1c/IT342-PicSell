package com.PicSell_IT342.PicSell.Repository;

import com.PicSell_IT342.PicSell.Model.NotificationModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationModel, Long> {
    List<NotificationModel> findByUserUserId(Long userId);
}