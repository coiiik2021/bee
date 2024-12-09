package com.beehat.repository;

import com.beehat.entity.ProductPromotion;
import com.beehat.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductPromotionRepo extends JpaRepository<ProductPromotion,Integer> {
    @Modifying
    @Transactional
    @Query("UPDATE ProductPromotion pp SET pp.status = :status WHERE pp.promotion.id = :promotionId")
    void updateStatusByPromotionId(@Param("promotionId") Integer promotionId, @Param("status") Byte status);
    List<ProductPromotion> findAllByPromotionAndStatus(Promotion promotion, byte status);
    List<ProductPromotion> findByPromotionId(Integer promotionId);
    void deleteByPromotionId(Integer promotionId);
}
