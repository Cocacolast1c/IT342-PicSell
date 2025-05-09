package com.PicSell_IT342.PicSell.Repository;

import com.PicSell_IT342.PicSell.Model.ImageModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
// You can remove the @Query import if not using custom queries
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ImageRepository extends JpaRepository<ImageModel, Long> {


    List<ImageModel> findByUploaderUserId(Long userId);

    List<ImageModel> findbyQuery(
            String imageNameQuery, String descriptionQuery, String tagsQuery);

}