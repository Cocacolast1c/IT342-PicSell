package com.PicSell_IT342.PicSell.Controller;

import com.PicSell_IT342.PicSell.Model.TransactionsModel;
import com.PicSell_IT342.PicSell.Service.TransactionService;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
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

    private static final String SUCCESS_URL  = "/transactions/success";
    private static final String CANCEL_URL   = "/transactions/cancel";


    @PostMapping("/pay")
    public String makePayment(@RequestParam double amount){

        try {
            Payment payment = transactionService.createPayment(
                    amount,
                    "USD",
                    "paypal",
                    "sale",
                    "payment description",
                    CANCEL_URL,
                    SUCCESS_URL);
            for(Links links : payment.getLinks()){
                if(links.getRel().equals("approval_url")){
                    return "Redirect to: "+links.getHref();

                }
            }
        } catch (PayPalRESTException e) {
            throw new RuntimeException(e);
        }

        return "Error processing the payment";

    }


    @GetMapping("/success")
    public String paymentSuccess(@RequestParam("paymentId") String paymentId , @RequestParam("PayerID") String payerId){


        try {
            Payment payment = transactionService.execute(paymentId, payerId);
            if(payment.getState().equals("approved")){
                return "payment is successfully done";
            }
        } catch (PayPalRESTException e) {
            throw new RuntimeException(e);
        }


        return  "payment failed";
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