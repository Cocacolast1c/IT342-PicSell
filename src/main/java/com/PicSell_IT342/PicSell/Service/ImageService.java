package com.PicSell_IT342.PicSell.Service;

import com.PicSell_IT342.PicSell.Model.ImageModel;
import com.PicSell_IT342.PicSell.Repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ImageService {
    @Autowired
    private ImageRepository imageRepository;

    public List<ImageModel> getAllImages() {
        return imageRepository.findAll();
    }

    public ImageModel getImageById(Long id) {
        return imageRepository.findById(id).orElse(null);
    }

    public List<ImageModel> getImagesByUserId(Long userId) {
        return imageRepository.findByUploaderUserId(userId);
    }

    public ImageModel updateImageDetails(Long id, ImageModel updatedImage) {
        Optional<ImageModel> optionalImage = imageRepository.findById(id);
        if (optionalImage.isPresent()) {
            ImageModel image = optionalImage.get();
            image.setImageName(updatedImage.getImageName());
            image.setImageDescription(updatedImage.getImageDescription());
            image.setLicenseType(updatedImage.getLicenseType());
            image.setPrice(updatedImage.getPrice());
            image.setTags(updatedImage.getTags());
            image.setImageFile(updatedImage.getImageFile());
            return imageRepository.save(image);
        }
        return null;
    }

    public ImageModel saveImage(ImageModel image) {
        return imageRepository.save(image);
    }

    public void deleteImage(Long id) {
        Optional<ImageModel> optionalImage = imageRepository.findById(id);
        if (optionalImage.isPresent()) {
            imageRepository.deleteById(id);
        }
    }
}