package com.PicSell_IT342.PicSell.Model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Blob;
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

    @Lob
    @Column(name = "image_file", columnDefinition = "LONGBLOB")
    private byte[] imageFile;

    @ManyToOne
    @JoinColumn(name = "uploader_id", nullable = false)
    private UserModel uploader;

    @OneToMany(mappedBy = "image", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("image-inventory")
    private Set<InventoryModel> inventory;

    @OneToMany(mappedBy = "image", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<TransactionsModel> transactions;
}