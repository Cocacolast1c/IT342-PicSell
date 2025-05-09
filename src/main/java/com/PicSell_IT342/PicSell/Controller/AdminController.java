
package com.PicSell_IT342.PicSell.Controller;

import com.PicSell_IT342.PicSell.Model.ImageModel;
import com.PicSell_IT342.PicSell.Model.TransactionsModel;
import com.PicSell_IT342.PicSell.Service.ImageService;
import com.PicSell_IT342.PicSell.Service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Autowired private ImageService imageService;
    @Autowired private TransactionService transactionService;


    @GetMapping("/user/{userId}/images")

    public List<ImageModel> getAllImagesByUserId(@PathVariable Long userId) {
        log.info("Admin request: Get images for user ID: {}", userId);
        // TODO: Map List<ImageModel> to List<ImageResponseDTO>
        return imageService.getImagesByUserId(userId);
    }


    @GetMapping("/images/all")
    public ResponseEntity<Page<ImageModel>> getAllImages(Pageable pageable) {
        log.info("Admin request: Get all images with pagination: {}", pageable);
        Page<ImageModel> imagePage = imageService.getAllImagesPaged(pageable);
        // TODO: Map Page<ImageModel> to Page<ImageResponseDTO> for consistency
        return ResponseEntity.ok(imagePage);
    }


    @GetMapping("/transactions/buyer/{buyerId}")
    public List<TransactionsModel> getTransactionsByBuyerId(@PathVariable Long buyerId) {
        log.info("Admin request: Get transactions for buyer ID: {}", buyerId);
        // TODO: Map List<TransactionsModel> to List<TransactionResponseDTO>
        return transactionService.getTransactionsByBuyerId(buyerId);
    }

    @GetMapping("/transactions/seller/{sellerId}")
    public List<TransactionsModel> getTransactionsBySellerId(@PathVariable Long sellerId) {
        log.info("Admin request: Get transactions for seller ID: {}", sellerId);
        // TODO: Map List<TransactionsModel> to List<TransactionResponseDTO>
        return transactionService.getTransactionsBySellerId(sellerId);
    }

}