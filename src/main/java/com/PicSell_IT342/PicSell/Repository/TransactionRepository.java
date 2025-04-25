package com.PicSell_IT342.PicSell.Repository;

import com.PicSell_IT342.PicSell.Model.TransactionsModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionsModel, Long> {
    List<TransactionsModel> findByBuyerUserId(Long buyerId);
    List<TransactionsModel> findBySellerUserId(Long sellerId);
}