package com.PicSell_IT342.PicSell.Controller;

import com.PicSell_IT342.PicSell.Model.ImageModel;
import com.PicSell_IT342.PicSell.Service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequestMapping("/images")
public class ImageController {
    @Autowired
    private ImageService imageService;

    @GetMapping
    public List<ImageModel> getAllImages(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
                                         @AuthenticationPrincipal OAuth2User oauth2User) throws GeneralSecurityException, IOException {
        return imageService.getAllImages(authorizedClient);
    }

    @GetMapping("/{id}")
    public ImageModel getImageById(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
                                   @AuthenticationPrincipal OAuth2User oauth2User,
                                   @PathVariable Long id) throws GeneralSecurityException, IOException {
        return imageService.getImageById(authorizedClient, id);
    }

    @GetMapping("/user/{userId}")
    public List<ImageModel> getImagesByUserId(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
                                              @AuthenticationPrincipal OAuth2User oauth2User,
                                              @PathVariable Long userId) throws GeneralSecurityException, IOException {
        return imageService.getImagesByUserId(authorizedClient, userId);
    }

    @PutMapping("/{id}")
    public ImageModel updateImageDetails(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
                                          @AuthenticationPrincipal OAuth2User oauth2User,
                                         @PathVariable Long id,
                                         @RequestBody ImageModel updatedImage) throws GeneralSecurityException, IOException {
        return imageService.updateImageDetails(authorizedClient, id, updatedImage);
    }

    @PostMapping
    public ImageModel createImage(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
                                  @AuthenticationPrincipal OAuth2User oauth2User,
                                  @RequestBody ImageModel image) throws GeneralSecurityException, IOException {
        return imageService.saveImage(authorizedClient, image);
    }

    @DeleteMapping("/{id}")
    public void deleteImage(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
                            @AuthenticationPrincipal OAuth2User oauth2User,
                            @PathVariable Long id) throws GeneralSecurityException, IOException {
        imageService.deleteImage(authorizedClient, id);
    }
}