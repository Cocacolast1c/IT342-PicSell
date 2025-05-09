package com.PicSell_IT342.PicSell.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponseDTO {
    private Long inventoryId;
    private InventoryImageInfoDTO image;
}