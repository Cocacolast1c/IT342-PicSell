package com.PicSell_IT342.PicSell.Controller;

import com.PicSell_IT342.PicSell.Model.ImageModel;
import com.PicSell_IT342.PicSell.Service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/images")
public class ImageController {
    @Autowired
    private ImageService imageService;

    @GetMapping
    public List<ImageModel> getAllImages() {
        return imageService.getAllImages();
    }

    @GetMapping("/{id}")
    public ImageModel getImageById(@PathVariable Long id) {
        return imageService.getImageById(id);
    }

    @GetMapping("/user/{userId}")
    public List<ImageModel> getImagesByUserId(@PathVariable Long userId) {
        return imageService.getImagesByUserId(userId);
    }

    @PutMapping("/{id}")
    public String updateImageDetails(@PathVariable Long id, @RequestBody ImageModel updatedImage) {
        try {
            ImageModel image = imageService.updateImageDetails(id, updatedImage);
            if (image == null) {
                return "Image not found";
            }
        } catch (Exception e) {
            return "Error updating image: " + e.getMessage();
        }
        return "Image details updated successfully";
    }

    @PostMapping
    public String createImage(@RequestBody ImageModel image) {
        try {
            imageService.saveImage(image);
        } catch (Exception e) {
            return "Error creating image: " + e.getMessage();
        }
        return "Image created successfully";
    }

    @DeleteMapping("/{id}")
    public String deleteImage(@PathVariable Long id) {
        try {
            imageService.deleteImage(id);
        } catch (Exception e) {
            return "Error deleting image: " + e.getMessage();
        }
        return "Image deleted successfully";
    }
}