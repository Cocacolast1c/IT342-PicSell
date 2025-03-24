package com.PicSell_IT342.PicSell.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
public class TransactionsModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    @JsonBackReference("user-boughtTransactions")
    private UserModel buyer;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    @JsonBackReference("user-soldTransactions")
    private UserModel seller;

    @ManyToOne
    @JoinColumn(name = "image_id")
    @JsonBackReference("image-transactions")
    private ImageModel image;

    private Date saleDate;
    private String saleState;
}