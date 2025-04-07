package com.PicSell_IT342.PicSell.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.Set;

@Entity
public class ImageModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ImageId;
    private String imageName;
    private String imageDescription;
    private String licenseType;
    private Double price;
    private String tags;

    @Lob
    @Column(name = "image_file", columnDefinition = "LONGBLOB")
    private byte[] imageFile;

    @ManyToOne
    @JoinColumn(name = "uploader_id", nullable = false)
    @JsonBackReference("user-images")
    private UserModel uploader;

    @OneToMany(mappedBy = "image", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("image-inventory")
    private Set<InventoryModel> inventory;

    @OneToMany(mappedBy = "image", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("image-transactions")
    private Set<TransactionsModel> transactions;

    public Long getImageId() {
        return ImageId;
    }

    public void setImageId(Long imageId) {
        ImageId = imageId;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageDescription() {
        return imageDescription;
    }

    public void setImageDescription(String imageDescription) {
        this.imageDescription = imageDescription;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public byte[] getImageFile() {
        return imageFile;
    }

    public void setImageFile(byte[] imageFile) {
        this.imageFile = imageFile;
    }

    public UserModel getUploader() {
        return uploader;
    }

    public void setUploader(UserModel uploader) {
        this.uploader = uploader;
    }

    public Set<InventoryModel> getInventory() {
        return inventory;
    }

    public void setInventory(Set<InventoryModel> inventory) {
        this.inventory = inventory;
    }

    public Set<TransactionsModel> getTransactions() {
        return transactions;
    }

    public void setTransactions(Set<TransactionsModel> transactions) {
        this.transactions = transactions;
    }
}