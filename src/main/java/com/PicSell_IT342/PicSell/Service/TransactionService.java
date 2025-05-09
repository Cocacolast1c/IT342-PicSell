package com.PicSell_IT342.PicSell.Service;

import com.PicSell_IT342.PicSell.Model.ImageModel;
import com.PicSell_IT342.PicSell.Model.TransactionsModel;
import com.PicSell_IT342.PicSell.Model.UserModel;
import com.PicSell_IT342.PicSell.Model.NotificationModel;
import com.PicSell_IT342.PicSell.Repository.TransactionRepository;
import com.PicSell_IT342.PicSell.exception.CustomExceptions.*;
import com.PicSell_IT342.PicSell.dto.TransactionResponseDTO; // Import DTO
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private APIContext apiContext;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private NotificationService notificationService;



    public List<TransactionsModel> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public TransactionsModel getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
    }

    public List<TransactionsModel> getTransactionsByBuyerId(Long buyerId) {
        return transactionRepository.findByBuyerUserId(buyerId);
    }

    public List<TransactionsModel> getTransactionsBySellerId(Long sellerId) {
        return transactionRepository.findBySellerUserId(sellerId);
    }

    @Transactional
    public TransactionsModel saveTransaction(TransactionsModel transaction) {
        if (transaction.getBuyer() == null || transaction.getBuyer().getUserId() == null) {
            throw new BadRequestException("Buyer with a valid ID must be provided.");
        }
        if (transaction.getSeller() == null || transaction.getSeller().getUserId() == null) {
            throw new BadRequestException("Seller with a valid ID must be provided.");
        }
        if (transaction.getImage() == null || transaction.getImage().getImageId() == null) {
            throw new BadRequestException("Image with a valid ID must be provided.");
        }

        transaction.setSaleState("PENDING");
        transaction.setSaleDate(LocalDateTime.now());

        log.info("Saving new transaction for buyer {}, seller {}, image {}",
                transaction.getBuyer().getUserId(), transaction.getSeller().getUserId(), transaction.getImage().getImageId());
        return transactionRepository.save(transaction);
    }

    @Transactional
    public TransactionResponseDTO updateSaleState(Long transactionId, String saleState) {
        TransactionsModel transaction = getTransactionById(transactionId); // Finds or throws ResourceNotFoundException
        log.info("Updating transaction {} state from {} to {}", transactionId, transaction.getSaleState(), saleState);
        transaction.setSaleState(saleState);
        TransactionsModel updatedTransaction = transactionRepository.save(transaction);
        return mapToTransactionResponseDTO(updatedTransaction);
    }

    public Payment createPayment(BigDecimal total, String currency, String method, String intent, String description, String cancelUrl, String successUrl) {
        Amount amount = new Amount();
        amount.setCurrency(currency);
        total = total.setScale(2, RoundingMode.HALF_UP);
        amount.setTotal(total.toString());

        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod(method);

        Payment payment = new Payment();
        payment.setIntent(intent);
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);
        payment.setRedirectUrls(redirectUrls);

        try {
            Payment createdPayment = payment.create(apiContext);
            log.debug("PayPal Payment Created. ID: {}, State: {}", createdPayment.getId(), createdPayment.getState());
            return createdPayment;
        } catch (PayPalRESTException e) {
            log.error("PayPal Error during payment creation: {}", e.getDetails(), e);
            throw new PaymentException("Error creating PayPal payment: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during payment creation: {}", e.getMessage(), e);
            throw new PaymentException("Unexpected error during payment creation.", e);
        }
    }

    public Payment execute(String paymentId, String payerId) {
        Payment payment = new Payment();
        payment.setId(paymentId);
        PaymentExecution paymentExecute = new PaymentExecution();
        paymentExecute.setPayerId(payerId);
        try {
            Payment executedPayment = payment.execute(apiContext, paymentExecute);
            log.debug("PayPal Payment Executed. ID: {}, State: {}", executedPayment.getId(), executedPayment.getState());
            return executedPayment;
        } catch (PayPalRESTException e) {
            log.error("PayPal Error during payment execution: {}", e.getDetails(), e);
            throw new PaymentException("Error executing PayPal payment: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during payment execution: {}", e.getMessage(), e);
            throw new PaymentException("Unexpected error during payment execution.", e);
        }
    }

    @Transactional
    public void linkTransactionToPaypal(Long transactionId, String paypalPaymentId, String paypalToken) {
        TransactionsModel transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId + " while linking PayPal ID."));

        if ("PENDING".equalsIgnoreCase(transaction.getSaleState())) {
            transaction.setPaypalPaymentId(paypalPaymentId);
            transaction.setPaypalToken(paypalToken);
            transactionRepository.save(transaction);
            log.debug("Linked Transaction ID {} to PayPal Payment ID {} and Token {}", transactionId, paypalPaymentId, paypalToken);
        } else {
            log.warn("Transaction ID {} was not PENDING when trying to link PayPal ID. Current State: {}", transactionId, transaction.getSaleState());
        }
    }

    @Transactional
    public TransactionsModel cancelTransactionByPaypalToken(String paypalToken) {
        log.debug("Attempting to cancel transaction for PayPal Token: {}", paypalToken);
        Optional<TransactionsModel> optionalTransaction = transactionRepository.findByPaypalToken(paypalToken);

        if (optionalTransaction.isPresent()) {
            TransactionsModel transaction = optionalTransaction.get();
            if ("PENDING".equalsIgnoreCase(transaction.getSaleState())) {
                transaction.setSaleState("CANCELLED");
                TransactionsModel updatedTransaction = transactionRepository.save(transaction);
                log.info("Transaction ID {} marked as CANCELLED due to PayPal cancellation callback (Token: {}).", transaction.getTransactionId(), paypalToken);
                return updatedTransaction;
            } else {
                log.warn("Transaction associated with PayPal Token {} was not PENDING when cancel callback received. Current State: {}", paypalToken, transaction.getSaleState());
                return transaction;
            }
        } else {
            log.warn("No PENDING transaction found for PayPal Token: {} in cancel callback.", paypalToken);
            return null;
        }
    }

    @Transactional
    public TransactionsModel completeTransactionByPaypalId(String paypalPaymentId) {
        log.debug("Attempting to complete transaction for PayPal Payment ID: {}", paypalPaymentId);
        TransactionsModel transaction = transactionRepository.findByPaypalPaymentId(paypalPaymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found for PayPal Payment ID: " + paypalPaymentId));

        if ("PENDING".equalsIgnoreCase(transaction.getSaleState())) {
            transaction.setSaleState("COMPLETED");
            transaction.setSaleDate(LocalDateTime.now());
            TransactionsModel updatedTransaction = transactionRepository.save(transaction);
            log.info("Transaction ID {} marked as COMPLETED.", transaction.getTransactionId());

            UserModel buyer = transaction.getBuyer();
            UserModel seller = transaction.getSeller();
            ImageModel image = transaction.getImage();

            Long sellerIdForNotification = null;
            String sellerUsernameForNotification = "a seller";
            boolean canNotifySeller = false;

            if (seller == null) {
                log.error("Seller object is NULL for transaction {}", transaction.getTransactionId());
            } else {
                try {

                    sellerIdForNotification = seller.getUserId();
                    if (sellerIdForNotification != null) {
                        sellerUsernameForNotification = seller.getUsername() != null ? seller.getUsername() : "ID: " + sellerIdForNotification; // Use username or ID
                        canNotifySeller = true;
                        log.debug("Seller details accessed for transaction {}: ID={}, Username={}",
                                transaction.getTransactionId(), sellerIdForNotification, sellerUsernameForNotification);
                    } else {
                        log.error("Seller object exists for transaction {} but getUserId() returned null.", transaction.getTransactionId());
                    }
                } catch (org.hibernate.LazyInitializationException lie) {
                    log.error("LazyInitializationException while accessing seller details for transaction {}. Need to ensure Seller is fetched eagerly.", transaction.getTransactionId(), lie);

                } catch (Exception e) {
                    log.error("Unexpected error accessing seller details for transaction {}: {}", transaction.getTransactionId(), e.getMessage(), e);
                }
            }

            if (buyer != null && buyer.getUserId() != null &&
                    canNotifySeller &&
                    image != null && image.getImageId() != null) {

                try {
                    inventoryService.addImageToUserInventory(buyer.getUserId(), image.getImageId());
                    log.info("Added Image ID {} to buyer's ({}) inventory for transaction {}.",
                            image.getImageId(), buyer.getUserId(), transaction.getTransactionId());

                    String buyerMessage = String.format("Purchase complete! You bought '%s'. It's now in your inventory.",
                            image.getImageName() != null ? image.getImageName() : "an image");
                    createAndSaveNotification(buyer, buyerMessage, transaction.getTransactionId());

                    String sellerMessage = String.format("Sale complete! Your image '%s' was purchased by %s.",
                            image.getImageName() != null ? image.getImageName() : "an image",
                            buyer.getUsername() != null ? buyer.getUsername() : "a user");
                    createAndSaveNotification(seller, sellerMessage, transaction.getTransactionId());

                } catch (BadRequestException be) {
                    log.warn("Could not add image {} to buyer's ({}) inventory for completed transaction {}: {}", image.getImageId(), buyer.getUserId(), transaction.getTransactionId(), be.getMessage());

                    String buyerMessage = String.format("Purchase complete for '%s'. Check your inventory.", image.getImageName() != null ? image.getImageName() : "an image");
                    createAndSaveNotification(buyer, buyerMessage, transaction.getTransactionId());
                    if (canNotifySeller) {
                        String sellerMessage = String.format("Sale complete! Your image '%s' was purchased by %s.", image.getImageName() != null ? image.getImageName() : "an image", buyer.getUsername() != null ? buyer.getUsername() : "a user");
                        createAndSaveNotification(seller, sellerMessage, transaction.getTransactionId());
                    }
                } catch (Exception e) {
                    log.error("Failed processing inventory/notification for completed transaction {}: {}", transaction.getTransactionId(), e.getMessage(), e);
                }
            } else {
                log.error("Cannot proceed with inventory/notification for transaction {}. Buyer valid: {}, Seller valid (canNotifySeller): {}, Image valid: {}",
                        transaction.getTransactionId(),
                        (buyer != null && buyer.getUserId() != null),
                        canNotifySeller,
                        (image != null && image.getImageId() != null));
                if (!canNotifySeller) {
                    log.error("Seller notification skipped because seller details could not be validated (ID: {}, Object Null: {}).", sellerIdForNotification, seller == null);
                }
            }
            return updatedTransaction;
        } else {
            log.warn("Transaction associated with PayPal ID {} was already processed. Current state: {}", paypalPaymentId, transaction.getSaleState());
            return transaction;
        }
    }

    private void createAndSaveNotification(UserModel user, String message, Long transactionId) {
        if (user == null || user.getUserId() == null) {
            log.error("Cannot create notification for transaction {}: User is null or has no ID.", transactionId);
            return;
        }
        try {
            NotificationModel notification = new NotificationModel();
            notification.setUser(user);
            notification.setMessage(message);
            notification.setTimestamp(new Date());
            notification.setRead(false);

            notificationService.saveNotification(notification);
            String userType = (user.getRole() != null && user.getRole().toUpperCase().contains("SELLER")) ? "seller" : "buyer";
            log.info("Created notification for {} ID {} regarding transaction {}.",
                    userType, user.getUserId(), transactionId);
        } catch (Exception notificationEx) {
            log.error("Failed to create/save notification for user ID {} regarding transaction {}: {}",
                    user.getUserId(), transactionId, notificationEx.getMessage(), notificationEx);
        }
    }

    private TransactionResponseDTO mapToTransactionResponseDTO(TransactionsModel transaction) {
        if (transaction == null) {
            return null;
        }
        Long buyerId = (transaction.getBuyer() != null) ? transaction.getBuyer().getUserId() : null;
        String buyerUsername = (transaction.getBuyer() != null) ? transaction.getBuyer().getUsername() : null;
        Long sellerId = (transaction.getSeller() != null) ? transaction.getSeller().getUserId() : null;
        String sellerUsername = (transaction.getSeller() != null) ? transaction.getSeller().getUsername() : null;
        Long imageId = (transaction.getImage() != null) ? transaction.getImage().getImageId() : null;
        String imageName = (transaction.getImage() != null) ? transaction.getImage().getImageName() : null;

        return new TransactionResponseDTO(
                transaction.getTransactionId(),
                transaction.getSaleDate(),
                transaction.getSaleState(),
                buyerId,
                buyerUsername,
                sellerId,
                sellerUsername,
                imageId,
                imageName
        );
    }
}