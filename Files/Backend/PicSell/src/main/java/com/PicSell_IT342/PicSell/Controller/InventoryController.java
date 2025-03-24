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
    public InventoryModel addImageToBuyerInventory(@RequestBody InventoryModel inventory) {
        return inventoryService.addImageToBuyerInventory(inventory.getUser().getUserId(), inventory.getImage().getImageId());
    }

    @DeleteMapping("/{id}")
    public void deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
    }


}