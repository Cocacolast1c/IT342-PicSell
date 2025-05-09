package com.PicSell_IT342.PicSell.Repository;

import com.PicSell_IT342.PicSell.Model.TransactionsModel;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional; // Import Optional

public interface TransactionRepository extends JpaRepository<TransactionsModel, Long> {


    @EntityGraph(attributePaths = {"buyer", "seller", "image", "image.uploader"})
    List<TransactionsModel> findByBuyerUserId(Long buyerId);

    @EntityGraph(attributePaths = {"buyer", "seller", "image", "image.uploader"})
    List<TransactionsModel> findBySellerUserId(Long sellerId);


    @EntityGraph(attributePaths = {"buyer", "seller", "image"})
    Optional<TransactionsModel> findByPaypalPaymentId(String paypalPaymentId);

    @EntityGraph(attributePaths = {"buyer", "seller", "image"})
    Optional<TransactionsModel> findByPaypalToken(String paypalToken);

}