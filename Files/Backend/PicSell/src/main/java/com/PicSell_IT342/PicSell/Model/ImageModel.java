package com.PicSell_IT342.PicSell.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
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
    private String driveFileId;

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
}