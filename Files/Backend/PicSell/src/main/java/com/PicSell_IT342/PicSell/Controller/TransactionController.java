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
    public TransactionsModel createTransaction(@RequestBody TransactionsModel transaction) {
        return transactionService.saveTransaction(transaction);
    }

    @PutMapping("/{transactionId}/sale-state")
    public TransactionsModel updateSaleState(@PathVariable Long transactionId, @RequestParam String saleState) {
        return transactionService.updateSaleState(transactionId, saleState);
    }


}