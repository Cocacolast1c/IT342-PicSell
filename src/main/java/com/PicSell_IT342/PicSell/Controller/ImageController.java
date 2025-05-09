package com.PicSell_IT342.PicSell.Controller;

import com.PicSell_IT342.PicSell.Model.ImageModel;
import com.PicSell_IT342.PicSell.Service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    @Autowired
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping
    public ResponseEntity<List<ImageModel>> getAllPublicImages() {
        try {
            List<ImageModel> images = imageService.findPublicImages();
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            System.err.println("Error fetching public images: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<ImageModel>> searchImages(
            @RequestParam("q") String query) {
        try {
            List<ImageModel> images = imageService.searchImages(query);
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            System.err.println("Error searching images: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<ImageModel> getImageById(@PathVariable Long id) {
        ImageModel image = imageService.getImageById(id);
        return ResponseEntity.ok(image);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ImageModel>> getImagesByUserId(@PathVariable Long userId) {
        List<ImageModel> images = imageService.getImagesByUserId(userId);
        return ResponseEntity.ok(images);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImageModel> updateImageDetails(@PathVariable Long id, @RequestBody ImageModel updatedImage) {
        ImageModel image = imageService.updateImageDetails(id, updatedImage);
        return ResponseEntity.ok(image);
    }

    @PostMapping
    public ResponseEntity<ImageModel> createImage(@RequestBody ImageModel image) {
        ImageModel createdImage = imageService.saveImage(image);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdImage);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        imageService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }
}