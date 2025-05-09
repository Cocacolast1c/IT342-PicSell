package com.PicSell_IT342.PicSell.Model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class TransactionsModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @Column(nullable = false)
    private LocalDateTime saleDate = LocalDateTime.now();

    @Column(nullable = false)
    private String saleState = "PENDING";


    @Column(unique = true, nullable = true)
    private String paypalPaymentId;

    @Column(nullable = true)
    private String paypalToken;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id")

    private UserModel buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")

    private UserModel seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private ImageModel image;

    public TransactionsModel() {
        this.saleDate = LocalDateTime.now();
        this.saleState = "PENDING";
    }

}