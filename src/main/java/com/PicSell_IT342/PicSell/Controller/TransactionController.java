
package com.PicSell_IT342.PicSell.Controller;

import com.PicSell_IT342.PicSell.Model.TransactionsModel;
import com.PicSell_IT342.PicSell.Service.TransactionService;
import com.PicSell_IT342.PicSell.Service.UserService;
import com.PicSell_IT342.PicSell.dto.TransactionResponseDTO;
import com.PicSell_IT342.PicSell.exception.CustomExceptions;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    @Autowired private TransactionService transactionService;
    @Autowired private UserService userService;

    @Value("${paypal.redirect.success.url:http://localhost:3000/payment-success}")
    private String frontendSuccessUrl;
    @Value("${paypal.redirect.cancel.url:http://localhost:3000/payment-cancel}")
    private String frontendCancelUrl;
    @Value("${paypal.callback.baseurl:http://localhost:8080}")
    private String callbackBaseUrl;

    @GetMapping
    public ResponseEntity<List<TransactionsModel>> getAllTransactions() {
        List<TransactionsModel> txs = transactionService.getAllTransactions();
        return ResponseEntity.ok(txs);
    }
    @GetMapping("/{id}")
    public ResponseEntity<TransactionsModel> getTransactionById(@PathVariable Long id) {
        TransactionsModel tx = transactionService.getTransactionById(id);
        return ResponseEntity.ok(tx);
    }
    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<TransactionsModel>> getTransactionsByBuyerId(@PathVariable Long buyerId) {
        List<TransactionsModel> txs = transactionService.getTransactionsByBuyerId(buyerId);
        return ResponseEntity.ok(txs);
    }
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<TransactionsModel>> getTransactionsBySellerId(@PathVariable Long sellerId) {
        List<TransactionsModel> txs = transactionService.getTransactionsBySellerId(sellerId);
        return ResponseEntity.ok(txs);
    }
    @PostMapping
    public ResponseEntity<TransactionsModel> createTransaction(@RequestBody TransactionsModel transaction) {
        log.info("Creating new transaction record (manual request)");
        TransactionsModel createdTx = transactionService.saveTransaction(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTx);
    }
    @PutMapping("/{transactionId}/sale-state")
    public ResponseEntity<TransactionResponseDTO> updateSaleState(@PathVariable Long transactionId, @RequestParam String saleState) {
        log.info("Request to update transaction {} state to {}", transactionId, saleState);
        TransactionResponseDTO dto = transactionService.updateSaleState(transactionId, saleState);
        return ResponseEntity.ok(dto);
    }
    @PostMapping("/pay")
    public ResponseEntity<PaymentApprovalResponse> makePayment(@RequestParam double amount, @RequestParam Long transactionId) {
        log.info("Received payment request for amount: {}, transactionId: {}", amount, transactionId);
        if (amount <= 0 || transactionId == null) { throw new CustomExceptions.BadRequestException("Amount must be positive and transactionId required."); }
        BigDecimal paymentAmount = BigDecimal.valueOf(amount);
        String backendSuccessUrl = callbackBaseUrl + "/transactions/success";
        String backendCancelUrl = callbackBaseUrl + "/transactions/cancel";
        try {
            Payment payment = transactionService.createPayment(paymentAmount, "USD", "paypal", "sale", "PicSell Image Purchase ID: " + transactionId, backendCancelUrl, backendSuccessUrl);
            String approvalUrl = null; String paypalToken = null;
            if (payment.getLinks() != null) { for (Links links : payment.getLinks()) { if ("approval_url".equals(links.getRel())) { approvalUrl = links.getHref(); Matcher matcher = Pattern.compile("[?&]token=([^&]+)").matcher(approvalUrl); if (matcher.find()) { paypalToken = matcher.group(1); } log.debug("Extracted approval URL: {} and Token: {}", approvalUrl, paypalToken); break; } } }
            if (approvalUrl != null && payment.getId() != null && paypalToken != null) { transactionService.linkTransactionToPaypal(transactionId, payment.getId(), paypalToken); return ResponseEntity.ok(new PaymentApprovalResponse(approvalUrl)); }
            else { String missing = (approvalUrl == null ? "URL " : "") + (payment.getId() == null ? "ID " : "") + (paypalToken == null ? "Token " : ""); log.error("Could not extract PayPal details: {} missing. State: {}", missing.trim(), payment.getState()); throw new CustomExceptions.PaymentException("Could not get required payment details from PayPal."); }
        } catch (CustomExceptions.PaymentException | CustomExceptions.BadRequestException | CustomExceptions.ResourceNotFoundException e) { throw e; }
        catch (Exception e) { log.error("Unexpected error during makePayment for transactionId {}: {}", transactionId, e.getMessage(), e); throw new CustomExceptions.PaymentException("Unexpected error initiating payment.", e); }
    }
    @GetMapping("/success")
    public RedirectView paymentSuccess(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId) {
        log.info("Received /success callback. PaymentID: {}, PayerID: {}", paymentId, payerId);
        String redirectUrl = frontendCancelUrl + "?reason=unknown_error";
        try {
            Payment payment = transactionService.execute(paymentId, payerId);
            if ("approved".equalsIgnoreCase(payment.getState())) { transactionService.completeTransactionByPaypalId(paymentId); redirectUrl = frontendSuccessUrl + "?paymentId=" + paymentId; log.info("Payment approved for {}. Redirecting to success.", paymentId); }
            else { log.warn("Payment not approved by PayPal. State: {}. PaymentID: {}", payment.getState(), paymentId); redirectUrl = frontendCancelUrl + "?reason=paypal_failed&paymentId=" + paymentId; }
        } catch(CustomExceptions.ResourceNotFoundException e) { log.error("Tx not found for PaymentID {} in /success: {}", paymentId, e.getMessage()); redirectUrl = frontendCancelUrl + "?reason=transaction_not_found&paymentId=" + paymentId; }
        catch (CustomExceptions.PaymentException | CustomExceptions.BadRequestException e) { log.error("Error executing/completing payment for PaymentID {}: {}", paymentId, e.getMessage(), e); redirectUrl = frontendCancelUrl + "?reason=execution_error&paymentId=" + paymentId; }
        catch (Exception e) { log.error("Unexpected error in /success for PaymentID {}: {}", paymentId, e.getMessage(), e); redirectUrl = frontendCancelUrl + "?reason=internal_error&paymentId=" + paymentId; }
        return new RedirectView(redirectUrl);
    }
    @GetMapping("/cancel")
    public RedirectView paymentCancel(@RequestParam(value = "token", required = false) String token) {
        log.info("Received /cancel callback. Token: {}", token);
        if (token != null && !token.isEmpty()) { try { transactionService.cancelTransactionByPaypalToken(token); } catch (Exception e) { log.error("Error processing cancel callback for token {}: {}", token, e.getMessage(), e); } }
        else { log.warn("Received /cancel callback without a token."); }
        String redirectUrl = frontendCancelUrl + "?reason=cancelled" + (token != null ? "&token=" + token : "");
        log.info("Redirecting to cancel URL: {}", redirectUrl);
        return new RedirectView(redirectUrl);
    }

    private static class PaymentApprovalResponse {
        private final String approvalUrl;
        public PaymentApprovalResponse(String approvalUrl) { this.approvalUrl = approvalUrl; }
        public String getApprovalUrl() { return approvalUrl; }
    }
}