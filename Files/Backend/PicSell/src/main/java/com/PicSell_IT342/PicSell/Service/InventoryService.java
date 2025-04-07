package com.PicSell_IT342.PicSell.Service;

import com.PicSell_IT342.PicSell.Model.ImageModel;
import com.PicSell_IT342.PicSell.Model.InventoryModel;
import com.PicSell_IT342.PicSell.Model.UserModel;
import com.PicSell_IT342.PicSell.Repository.ImageRepository;
import com.PicSell_IT342.PicSell.Repository.InventoryRepository;
import com.PicSell_IT342.PicSell.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {
    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageRepository imageRepository;

    public List<InventoryModel> getImagesByUserId(Long userId) {
        Optional<UserModel> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found!");
        }
        return inventoryRepository.findImagesByUser(userOpt.get());
    }

    public InventoryModel addImageToBuyerInventory(Long userId, Long imageId) {
        UserModel buyer = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found!"));

        ImageModel image = imageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image with ID " + imageId + " not found!"));

        if (inventoryRepository.existsByUserAndImage(buyer, image)) {
            throw new IllegalStateException("User already owns this image!");
        }

        InventoryModel inventory = new InventoryModel();
        inventory.setUser(buyer);
        inventory.setImage(image);
        return inventoryRepository.save(inventory);
    }


    public void deleteInventory(Long id) {
        inventoryRepository.deleteById(id);
    }
}