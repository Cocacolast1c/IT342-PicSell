package com.PicSell_IT342.PicSell.Controller;

import com.PicSell_IT342.PicSell.Model.TransactionsModel;
import com.PicSell_IT342.PicSell.Service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;

    @GetMapping
    public List<TransactionsModel> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/{id}")
    public TransactionsModel getTransactionById(@PathVariable Long id) {
        return transactionService.getTransactionById(id);
    }

    @GetMapping("/buyer/{buyerId}")
    public List<TransactionsModel> getTransactionsByBuyerId(@PathVariable Long buyerId) {
        return transactionService.getTransactionsByBuyerId(buyerId);
    }
    @GetMapping("/seller/{sellerId}")
    public List<TransactionsModel> getTransactionsBySellerId(@PathVariable Long sellerId) {
        return transactionService.getTransactionsBySellerId(sellerId);
    }

    @PostMapping
    public String createTransaction(@RequestBody TransactionsModel transaction) {
        try {
            if (transaction.getBuyer() == null || transaction.getSeller() == null || transaction.getImage() == null) {
                return "Buyer, Seller, or Image cannot be null";
            }
            if (transaction.getBuyer().getUserId() == null || transaction.getSeller().getUserId() == null || transaction.getImage().getImageId() == null) {
                return "Buyer ID, Seller ID, or Image ID cannot be null";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
        transactionService.saveTransaction(transaction);
        return "Transaction created successfully";
    }

    @PutMapping("/{transactionId}/sale-state")
    public String updateSaleState(@PathVariable Long transactionId, @RequestParam String saleState) {
        try {
            if (transactionId == null || saleState == null) {
                return "Transaction ID or Sale State cannot be null";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
        transactionService.updateSaleState(transactionId, saleState);
        return "Sale state updated successfully";
    }


}