package com.PicSell_IT342.PicSell.Controller;

import com.PicSell_IT342.PicSell.Model.ImageModel;
import com.PicSell_IT342.PicSell.Model.TransactionsModel;
import com.PicSell_IT342.PicSell.Service.ImageService;
import com.PicSell_IT342.PicSell.Service.TransactionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ImageService imageService;
    private final TransactionService transactionService;

    public AdminController(ImageService imageService, TransactionService transactionService) {
        this.imageService = imageService;
        this.transactionService = transactionService;
    }

    @GetMapping("/user/{userId}")
    public List<ImageModel> getAllImagesByUserId(@PathVariable Long userId) {
        System.out.println("Logged in user authorities: " +
                SecurityContextHolder.getContext().getAuthentication().getAuthorities());

        System.out.println("Current authorities: " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());

        return imageService.getImagesByUserId(userId);
    }

    @GetMapping("/buyer/{buyerId}")
    public List<TransactionsModel> getTransactionsByBuyerId(@PathVariable Long buyerId) {
        return transactionService.getTransactionsByBuyerId(buyerId);
    }
    @GetMapping("/seller/{sellerId}")
    public List<TransactionsModel> getTransactionsBySellerId(@PathVariable Long sellerId) {
        return transactionService.getTransactionsBySellerId(sellerId);
    }

}
