package com.PicSell_IT342.PicSell.Controller;

import com.PicSell_IT342.PicSell.dto.InventoryResponseDTO;
import com.PicSell_IT342.PicSell.Model.InventoryModel;
import com.PicSell_IT342.PicSell.Service.InventoryService;
import com.PicSell_IT342.PicSell.exception.CustomExceptions; // If needed for other methods
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<InventoryResponseDTO>> getUserInventory(@PathVariable Long userId) {
        try {
            List<InventoryResponseDTO> inventoryDTOs = inventoryService.getInventoryByUserId(userId);
            return ResponseEntity.ok(inventoryDTOs);
        } catch (Exception e) {
            System.err.println("[ERROR] Failed fetching inventory DTOs for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/add")
    public ResponseEntity<String> addImageToInventoryEndpoint(@RequestBody InventoryModel inventory) {
        try {
            if (inventory.getUser() == null || inventory.getImage() == null || inventory.getUser().getUserId() == null || inventory.getImage().getImageId() == null) {
                return ResponseEntity.badRequest().body("User ID or Image ID cannot be null");
            }
            inventoryService.addImageToUserInventory(inventory.getUser().getUserId(), inventory.getImage().getImageId());
            return ResponseEntity.status(HttpStatus.CREATED).body("Image added to inventory successfully");
        } catch (CustomExceptions.BadRequestException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            System.err.println("[ERROR] Failed adding to inventory via controller: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding item to inventory: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteInventory(@PathVariable Long id) {
        try {
            inventoryService.deleteInventory(id);
            return ResponseEntity.ok("Inventory item deleted successfully");
        } catch (CustomExceptions.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            System.err.println("[ERROR] Failed deleting inventory item " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting inventory item.");
        }
    }
}