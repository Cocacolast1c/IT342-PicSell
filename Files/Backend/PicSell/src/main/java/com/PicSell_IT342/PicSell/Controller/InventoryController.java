package com.PicSell_IT342.PicSell.Controller;

import com.PicSell_IT342.PicSell.Model.ImageModel;
import com.PicSell_IT342.PicSell.Model.InventoryModel;
import com.PicSell_IT342.PicSell.Service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventories")
public class InventoryController {
    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/user/{userId}/images")
    public List<InventoryModel> getImagesByUserId(@PathVariable Long userId) {
        return inventoryService.getImagesByUserId(userId);
    }

    @PostMapping("/add")
    public String addImageToBuyerInventory(@RequestBody InventoryModel inventory) {
        try {
            if (inventory.getUser() == null || inventory.getImage() == null) {
                return "User or Image cannot be null";
            }
            if (inventory.getUser().getUserId() == null || inventory.getImage().getImageId() == null) {
                return "User ID or Image ID cannot be null";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
        inventoryService.addImageToBuyerInventory(inventory.getUser().getUserId(), inventory.getImage().getImageId());
        return "Image added to inventory successfully";

    }

    @DeleteMapping("/{id}")
    public String deleteInventory(@PathVariable Long id) {
        try {
            inventoryService.deleteInventory(id);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
        return "Inventory deleted successfully";
    }


}