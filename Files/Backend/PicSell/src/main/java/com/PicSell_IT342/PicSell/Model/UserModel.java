package com.PicSell_IT342.PicSell.Model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.Set;


@Entity
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    private String username;
    private String password;
    private String email;

    @Lob
    @Column(name = "image_file", columnDefinition = "LONGBLOB")
    private byte[] imagefile;

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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public byte[] getImagefile() {
        return imagefile;
    }

    public void setImagefile(byte[] imagefile) {
        this.imagefile = imagefile;
    }

    public Set<ImageModel> getUploadedImages() {
        return uploadedImages;
    }

    public void setUploadedImages(Set<ImageModel> uploadedImages) {
        this.uploadedImages = uploadedImages;
    }

    public Set<InventoryModel> getInventory() {
        return inventory;
    }

    public void setInventory(Set<InventoryModel> inventory) {
        this.inventory = inventory;
    }

    public Set<TransactionsModel> getBoughtTransactions() {
        return boughtTransactions;
    }

    public void setBoughtTransactions(Set<TransactionsModel> boughtTransactions) {
        this.boughtTransactions = boughtTransactions;
    }

    public Set<TransactionsModel> getSoldTransactions() {
        return soldTransactions;
    }

    public void setSoldTransactions(Set<TransactionsModel> soldTransactions) {
        this.soldTransactions = soldTransactions;
    }

    public Set<NotificationModel> getNotifications() {
        return notifications;
    }

    public void setNotifications(Set<NotificationModel> notifications) {
        this.notifications = notifications;
    }
}
