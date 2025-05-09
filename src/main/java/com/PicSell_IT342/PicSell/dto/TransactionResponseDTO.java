package com.PicSell_IT342.PicSell.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDTO {
    private Long transactionId;
    private LocalDateTime saleDate;
    private String saleState;
    private Long buyerId;
    private String buyerUsername;
    private Long sellerId;
    private String sellerUsername;
    private Long imageId;
    private String imageName;

}