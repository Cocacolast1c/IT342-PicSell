package com.PicSell_IT342.PicSell.Repository;

import com.PicSell_IT342.PicSell.Model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserModel, Long> {
    UserModel findByUsername(String username);
    UserModel findByEmail(String email);

    @Query("SELECT u FROM UserModel u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<UserModel> findByUsernameOrEmail(@Param("identifier") String identifier);
}