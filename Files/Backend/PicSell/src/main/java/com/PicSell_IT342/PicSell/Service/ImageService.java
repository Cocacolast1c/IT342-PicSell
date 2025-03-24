package com.PicSell_IT342.PicSell.Service;

import com.PicSell_IT342.PicSell.Model.ImageModel;
import com.PicSell_IT342.PicSell.Repository.ImageRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ImageService {
    @Autowired
    private ImageRepository imageRepository;

    // Drive Api Service
    /*
    @Autowired
    private Drive driveService;
    */

    private GoogleCredential getGoogleCredential(OAuth2AuthorizedClient authorizedClient) throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new GoogleCredential().setAccessToken(authorizedClient.getAccessToken().getTokenValue());
    }

    public List<ImageModel> getAllImages(OAuth2AuthorizedClient authorizedClient) throws GeneralSecurityException, IOException {
        GoogleCredential credential = getGoogleCredential(authorizedClient);

        return imageRepository.findAll();
    }

    public ImageModel getImageById(OAuth2AuthorizedClient authorizedClient, Long id) throws GeneralSecurityException, IOException {
        GoogleCredential credential = getGoogleCredential(authorizedClient);

        return imageRepository.findById(id).orElse(null);
    }

    public List<ImageModel> getImagesByUserId(OAuth2AuthorizedClient authorizedClient, Long userId) throws GeneralSecurityException, IOException {
        GoogleCredential credential = getGoogleCredential(authorizedClient);

        return imageRepository.findByUploaderUserId(userId);
    }

    public ImageModel updateImageDetails(OAuth2AuthorizedClient authorizedClient, Long id, ImageModel updatedImage) throws GeneralSecurityException, IOException {

        GoogleCredential credential = getGoogleCredential(authorizedClient);

        Optional<ImageModel> optionalImage = imageRepository.findById(id);
        if (optionalImage.isPresent()) {
            ImageModel image = optionalImage.get();
            image.setImageName(updatedImage.getImageName());
            image.setImageDescription(updatedImage.getImageDescription());
            image.setLicenseType(updatedImage.getLicenseType());
            image.setPrice(updatedImage.getPrice());
            image.setTags(updatedImage.getTags());
            return imageRepository.save(image);
        }
        return null;
    }

    public ImageModel saveImage(OAuth2AuthorizedClient authorizedClient, ImageModel image) throws GeneralSecurityException, IOException {

        GoogleCredential credential = getGoogleCredential(authorizedClient);

        ImageModel savedImage = imageRepository.save(image);

        // Save Operation when using Google Drive API
        /*

        FrontEnd Upload Logic Place Here

        File file= new File();
        file.setName(filePath.getName());
        if (folderId != null) {
            file.setParents(Collections.singletonList(folderId));
        }
        FileContent mediaContent = new FileContent(mimeType, filePath);
        File uploadedFile = driveService.files().create(file, mediaContent)
                .setFields("id, name, webViewLink, webContentLink")
                .execute();
        savedImage.setDriveFileId(uploadedFile.getId());
        imageRepository.save(savedImage);
        */

        return savedImage;
    }

    public void deleteImage(OAuth2AuthorizedClient authorizedClient, Long id) throws GeneralSecurityException, IOException {
        GoogleCredential credential = getGoogleCredential(authorizedClient);

        Optional<ImageModel> optionalImage = imageRepository.findById(id);
        if (optionalImage.isPresent()) {
            ImageModel image = optionalImage.get();
            String driveFileId = image.getDriveFileId();
            imageRepository.deleteById(id);

            // Delete Operation when using Google Drive API
            /*
            if (driveFileId != null) {
                driveService.files().delete(driveFileId).execute();
            }
            */
        }
    }
}