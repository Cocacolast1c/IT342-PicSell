package com.PicSell_IT342.PicSell.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
public class NotificationModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference("user-notifications")
    private UserModel user;

    private String message;
    private Date timestamp;
    private boolean isRead = false;

    // Getters and Setters
}
