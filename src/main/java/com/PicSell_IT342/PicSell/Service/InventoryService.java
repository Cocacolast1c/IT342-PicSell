package com.PicSell_IT342.PicSell.Service;

import com.PicSell_IT342.PicSell.Model.ImageModel;
import com.PicSell_IT342.PicSell.Model.InventoryModel;
import com.PicSell_IT342.PicSell.Model.UserModel;
import com.PicSell_IT342.PicSell.Repository.InventoryRepository;
import com.PicSell_IT342.PicSell.Repository.UserRepository;
import com.PicSell_IT342.PicSell.Repository.ImageRepository;
// --- ADDED: Import DTOs ---
import com.PicSell_IT342.PicSell.dto.InventoryImageInfoDTO;
import com.PicSell_IT342.PicSell.dto.InventoryResponseDTO;
import com.PicSell_IT342.PicSell.dto.UploaderInfoDTO;
// --- END ADDED ---
import com.PicSell_IT342.PicSell.exception.CustomExceptions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64; // Import Base64 encoder
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Import Collectors

@Service
public class InventoryService {
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ImageRepository imageRepository;


    public List<InventoryResponseDTO> getInventoryByUserId(Long userId) {
        List<InventoryModel> inventoryList = inventoryRepository.findByUserUserId(userId);

        return inventoryList.stream()
                .map(this::mapToInventoryResponseDTO)
                .collect(Collectors.toList());
    }


    private InventoryResponseDTO mapToInventoryResponseDTO(InventoryModel item) {
        if (item == null) { return null; }

        InventoryImageInfoDTO imageInfo = null;
        ImageModel image = item.getImage();

        if (image != null) {
            UploaderInfoDTO uploaderInfo = null;
            UserModel uploader = image.getUploader();

            if (uploader != null) {
                uploaderInfo = new UploaderInfoDTO(uploader.getUserId(), uploader.getUsername());
            }

            String imageFileBase64 = (image.getImageFile() != null) ? Base64.getEncoder().encodeToString(image.getImageFile()) : null;

            imageInfo = new InventoryImageInfoDTO(
                    image.getImageId(),
                    image.getImageName(),
                    imageFileBase64,
                    image.getPrice(),
                    uploaderInfo,
                    image.getTags(),
                    image.getLicenseType()
            );
        }

        return new InventoryResponseDTO(
                item.getInventoryId(),
                imageInfo
        );
    }

    @Transactional
    public InventoryModel addImageToUserInventory(Long userId, Long imageId) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        ImageModel image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with ID: " + imageId));
        if (inventoryRepository.existsByUserAndImage(user, image)) {
            throw new BadRequestException("User already owns this image (Item already in inventory)!");
        }
        InventoryModel inventoryItem = new InventoryModel();
        inventoryItem.setUser(user);
        inventoryItem.setImage(image);
        return inventoryRepository.save(inventoryItem);
    }
    @Transactional
    public void deleteInventory(Long id) {
        if (!inventoryRepository.existsById(id)) { throw new ResourceNotFoundException("Inventory item not found with ID: " + id); }
        inventoryRepository.deleteById(id);
    }

}