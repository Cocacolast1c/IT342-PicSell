package com.PicSell_IT342.PicSell.Model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    private String username;
    private String password;
    private String email;

    @OneToMany(mappedBy = "uploader", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("user-images")
    private Set<ImageModel> uploadedImages;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("user-inventory")
    private Set<InventoryModel> inventory;

    @OneToMany(mappedBy = "buyer")
    @JsonManagedReference("user-boughtTransactions")
    private Set<TransactionsModel> boughtTransactions;

    @OneToMany(mappedBy = "seller")
    @JsonManagedReference("user-soldTransactions")
    private Set<TransactionsModel> soldTransactions;

    @OneToMany(mappedBy = "user")
    @JsonManagedReference("user-notifications")
    private Set<NotificationModel> notifications;

    // Getters and Setters
}
