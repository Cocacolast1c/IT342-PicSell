package com.PicSell_IT342.PicSell.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryImageInfoDTO {
    private Long imageId;
    private String imageName;
    private String imageFile;
    private Double price;
    private UploaderInfoDTO uploader;
    private String tags;
    private String licenseType;
}