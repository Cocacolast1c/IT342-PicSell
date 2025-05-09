package com.PicSell_IT342.PicSell.Service;

import com.PicSell_IT342.PicSell.Model.ImageModel;
import com.PicSell_IT342.PicSell.Repository.ImageRepository;
import com.PicSell_IT342.PicSell.exception.CustomExceptions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

@Service
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);

    @Autowired private ImageRepository imageRepository;
    @Autowired private InventoryService inventoryService;
    @Autowired private UserService userService;

    public List<ImageModel> findPublicImages() {
        return imageRepository.findAll();
    }

    public Page<ImageModel> getAllImagesPaged(Pageable pageable) {
        log.info("Admin request: Fetching all images, page {} size {}", pageable.getPageNumber(), pageable.getPageSize());
        return imageRepository.findAll(pageable);
    }

    public ImageModel getImageById(Long id) {
        return imageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + id));
    }

    public List<ImageModel> getImagesByUserId(Long userId) {
        return imageRepository.findByUploaderUserId(userId);
    }

    public List<ImageModel> searchImages(String query) {
        if (query == null || query.trim().isEmpty()) { return findPublicImages(); }
        String trimmedQuery = query.trim();
        log.debug("Searching images for query: '{}'", trimmedQuery);
        return imageRepository.findDistinctByImageNameContainingIgnoreCaseOrImageDescriptionContainingIgnoreCaseOrTagsContainingIgnoreCase(
                trimmedQuery, trimmedQuery, trimmedQuery);
    }

    @Transactional
    public ImageModel saveImage(ImageModel image) {
        if (image.getUploader() == null || image.getUploader().getUserId() == null) { throw new BadRequestException("Image must have an associated uploader with a valid ID."); }
        if (image.getImageName() == null || image.getImageName().trim().isEmpty()) { throw new BadRequestException("Image name cannot be empty."); }
        if (image.getPrice() == null || image.getPrice() < 0) { throw new BadRequestException("Image price must be provided and cannot be negative."); }
        if (image.getImageFile() == null || image.getImageFile().length == 0) { throw new BadRequestException("Image file content cannot be empty."); }

        ImageModel savedImage = imageRepository.save(image);
        log.info("Saved new Image ID: {} for User ID: {}", savedImage.getImageId(), savedImage.getUploader().getUserId());

        if (savedImage.getImageId() != null && savedImage.getUploader() != null && savedImage.getUploader().getUserId() != null) {
            Long userId = savedImage.getUploader().getUserId();
            Long imageId = savedImage.getImageId();
            try {
                inventoryService.addImageToUserInventory(userId, imageId);
                log.info("Automatically added image {} to inventory for uploader {}", imageId, userId);
            } catch (BadRequestException be) {
                log.warn("Could not automatically add image {} to inventory for uploader {}: {}", imageId, userId, be.getMessage());
            } catch (Exception e) {
                log.error("Failed to automatically add image {} to uploader's ({}) inventory after upload: {}", imageId, userId, e.getMessage(), e);
            }
        } else { log.error("Cannot add image to inventory automatically due to missing Image ID or Uploader ID after save."); }
        return savedImage;
    }

    @Transactional
    public ImageModel updateImageDetails(Long id, ImageModel updatedImageMetadata) {
        ImageModel image = getImageById(id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!hasPermissionToModify(authentication, image)) {
            log.warn("Access denied for user '{}' trying to update image {}", authentication != null ? authentication.getName() : "NULL_AUTH", id);
            throw new AccessDeniedException("User does not have permission to update this image.");
        }

        if (updatedImageMetadata.getImageName() == null || updatedImageMetadata.getImageName().trim().isEmpty()) { throw new BadRequestException("Image name cannot be empty."); }
        if (updatedImageMetadata.getPrice() != null && updatedImageMetadata.getPrice() < 0) { throw new BadRequestException("Image price cannot be negative."); }
        image.setImageName(updatedImageMetadata.getImageName());
        image.setImageDescription(updatedImageMetadata.getImageDescription());
        image.setLicenseType(updatedImageMetadata.getLicenseType());
        image.setPrice(updatedImageMetadata.getPrice());
        image.setTags(updatedImageMetadata.getTags());
        log.info("Updating details for image ID: {} by user '{}'", id, authentication.getName());
        return imageRepository.save(image);
    }

    @Transactional
    public void deleteImage(Long id) {
        ImageModel image = getImageById(id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!hasPermissionToModify(authentication, image)) {
            log.warn("Access denied for user '{}' trying to delete image {}", authentication != null ? authentication.getName() : "NULL_AUTH", id);
            throw new AccessDeniedException("User does not have permission to delete this image.");
        }

        log.warn("Deleting image ID: {} uploaded by user ID: {} (Action performed by user '{}')",
                id, image.getUploader() != null ? image.getUploader().getUserId() : "UNKNOWN", authentication.getName());
        imageRepository.delete(image);
        log.info("Successfully deleted Image ID: {}", id);
    }

    private boolean hasPermissionToModify(Authentication authentication, ImageModel image) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Permission check failed for image {}: Unauthenticated user.", image != null ? image.getImageId() : "NULL_IMAGE");
            return false;
        }
        if (image == null || image.getUploader() == null || image.getUploader().getUserId() == null) {
            log.warn("Permission check failed: Image ({}) or its uploader/uploaderId is null.", image != null ? image.getImageId() : "NULL_IMAGE");
            return false;
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> "ROLE_ADMIN".equals(grantedAuthority.getAuthority()));

        if (isAdmin) {
            log.debug("Permission granted for image {}: User '{}' is ADMIN", image.getImageId(), authentication.getName());
            return true;
        }

        Long currentUserId = userService.getCurrentUserId();
        if (currentUserId == null) {
            log.error("Permission check failed for image {}: Could not determine current user ID from principal for user '{}'",
                    image.getImageId(), authentication.getName());
            return false;
        }

        boolean isOwner = Objects.equals(currentUserId, image.getUploader().getUserId());
        log.debug("Permission check for image {}: Current User ID={}, Owner ID={}, Is Owner={}",
                image.getImageId(), currentUserId, image.getUploader().getUserId(), isOwner);

        return isOwner;
    }
}