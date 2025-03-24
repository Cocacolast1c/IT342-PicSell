package com.PicSell_IT342.PicSell.Service;

import com.PicSell_IT342.PicSell.Model.TransactionsModel;
import com.PicSell_IT342.PicSell.Repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;

    public List<TransactionsModel> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public TransactionsModel getTransactionById(Long id) {
        return transactionRepository.findById(id).orElse(null);
    }

    public List<TransactionsModel> getTransactionsByBuyerId(Long buyerId) {
        return transactionRepository.findByBuyerUserId(buyerId);
    }
    public List<TransactionsModel> getTransactionsBySellerId(Long sellerId) {
        return transactionRepository.findBySellerUserId(sellerId);
    }

    public TransactionsModel saveTransaction(TransactionsModel transaction) {
        return transactionRepository.save(transaction);
    }

    public TransactionsModel updateSaleState(Long transactionId, String saleState) {
        TransactionsModel transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found!"));
        transaction.setSaleState(saleState);
        return transactionRepository.save(transaction);
    }

}