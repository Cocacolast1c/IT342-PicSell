package com.PicSell_IT342.PicSell.Repository;

import com.PicSell_IT342.PicSell.Model.ImageModel;
import com.PicSell_IT342.PicSell.Model.InventoryModel;
import com.PicSell_IT342.PicSell.Model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryRepository extends JpaRepository<InventoryModel, Long> {
    List<InventoryModel> findImagesByUser(UserModel user);
    boolean existsByUserAndImage(UserModel user, ImageModel image);
}