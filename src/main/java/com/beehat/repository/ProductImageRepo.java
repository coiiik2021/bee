package com.beehat.repository;

import com.beehat.entity.ProductImage;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductImageRepo extends JpaRepository<ProductImage, Integer> {
    List<ProductImage> findByProductId(Integer productId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ProductImage img WHERE img.product.id = :productId")
    void deleteByProductId(@Param("productId") Integer productId);
}
