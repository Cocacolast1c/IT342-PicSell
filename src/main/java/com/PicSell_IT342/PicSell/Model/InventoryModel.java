package com.PicSell_IT342.PicSell.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class InventoryModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-inventory")
    private UserModel user;

    @ManyToOne
    @JoinColumn(name = "image_id", nullable = false)
    @JsonBackReference("image-inventory")
    private ImageModel image;


    @Transient
    private Long imageId;

    @Transient
    private Long userId;

    @PostLoad
    private void postLoad() {
        this.imageId = image.getImageId();
        this.userId = user.getUserId();
    }

    public Long getInventoryId() {
        return id;
    }
}