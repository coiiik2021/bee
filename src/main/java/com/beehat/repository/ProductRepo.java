package com.beehat.repository;

import com.beehat.entity.Product;
import com.beehat.entity.ProductDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductRepo extends JpaRepository<Product, Integer> {
    @Query("select p from Product p where p.name like ?1")
    public List<Product> findByName(String keyword);
    Page<Product> findAll(Pageable pageable);
    Page<Product> findByPromotionIsNull(Pageable pageable);
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.promotion = null WHERE p.promotion.id = :promotionId")
    void clearPromotionByPromotionId(@Param("promotionId") Integer promotionId);
    @Query("SELECT p FROM Product p WHERE p.promotion IS NULL OR p.promotion.id = :promotionId")
    Page<Product> findProductUpdate(@Param("promotionId") Integer promotionId, Pageable pageable);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, int id);
    Product findBySku(String sku);
    List<Product> findTop12ByOrderByCreatedDateDesc();
    List<Product> findByPromotionIdNotNull();
    @Query("SELECT P FROM Product P " +
            "WHERE (:name IS NULL OR P.name LIKE CONCAT('%', :name, '%')) " +
            "AND (:categoryId IS NULL OR P.category.id = :categoryId) " +
            "AND (:materialId IS NULL OR P.material.id = :materialId) " +
            "AND (:styleId IS NULL OR P.style.id = :styleId) " +
            "AND (:liningId IS NULL OR P.lining.id = :liningId) " +
            "AND (:beltId IS NULL OR P.belt.id = :beltId)")
    Page<Product> findByCriteria(
            String name ,Integer categoryId, Integer materialId, Integer styleId, Integer liningId, Integer beltId,Pageable pageable
    );

}
