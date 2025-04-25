package com.PicSell_IT342.PicSell.Service;

import com.PicSell_IT342.PicSell.Model.TransactionsModel;
import com.PicSell_IT342.PicSell.Repository.TransactionRepository;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private APIContext apiContext;

    public Payment createPayment(Double total, String currency, String method,
                                 String intent , String description , String cancelUrl , String successUrl) throws PayPalRESTException {

        Amount amount = new Amount();
        amount.setCurrency(currency);
        amount.setTotal(String.format("%.2f",total));

        //Transaction
        Transaction transaction =new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);
        List<Transaction> transactions = new ArrayList<Transaction>();
        transactions.add(transaction);

        //Payer
        Payer payer = new Payer();
        payer.setPaymentMethod(method.toUpperCase());

        Payment payment  = new Payment();
        payment.setIntent(intent);
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);
        payment.setRedirectUrls(redirectUrls);

        return payment.create(apiContext);


    }

    public Payment execute(String paymentId , String payerId) throws PayPalRESTException {

        Payment payment = new Payment();
        payment.setId(paymentId);
        PaymentExecution  paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);
        return payment.execute(apiContext,paymentExecution);
    }

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