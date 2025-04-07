package com.PicSell_IT342.PicSell.Repository;

import com.PicSell_IT342.PicSell.Model.ImageModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<ImageModel, Long> {
    List<ImageModel> findByUploaderUserId(Long userId);
}