package com.beehat.repository;

import com.beehat.entity.Employee;
import com.beehat.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromotionRepo extends JpaRepository<Promotion,Integer> {
    @Query("SELECT p FROM Promotion p WHERE p.discountAmount > 0 AND "
            + "(:search IS NULL OR :search = '' OR p.name LIKE %:search%) AND "
            + "(:fromDate IS NULL OR p.startDate >= :fromDate) AND "
            + "(:toDate IS NULL OR p.endDate <= :toDate) AND "
            + "(:status IS NULL OR p.status = :status)")
    List<Promotion> findPromotionsByDiscountAmountGreaterThan(@Param("search") String search,
                                                              @Param("fromDate") LocalDateTime fromDate,
                                                              @Param("toDate") LocalDateTime toDate,
                                                              @Param("status") Byte status);

    @Query("SELECT p FROM Promotion p WHERE p.discountPercentage > 0 AND "
            + "(:search IS NULL OR :search = '' OR p.name LIKE %:search%) AND "
            + "(:fromDate IS NULL OR p.startDate >= :fromDate) AND "
            + "(:toDate IS NULL OR p.endDate <= :toDate) AND "
            + "(:status IS NULL OR p.status = :status)")
    List<Promotion> findPromotionsByDiscountPercentageGreaterThan(@Param("search") String search,
                                                                  @Param("fromDate") LocalDateTime fromDate,
                                                                  @Param("toDate") LocalDateTime toDate,
                                                                  @Param("status") Byte status);
    @Query("SELECT p FROM Promotion p WHERE "
            + "(:search IS NULL OR :search = '' OR p.name LIKE %:search%) AND "
            + "(:fromDate IS NULL OR p.startDate >= :fromDate) AND "
            + "(:toDate IS NULL OR p.endDate <= :toDate) AND "
            + "(:status IS NULL OR p.status = :status)")
    List<Promotion> findAllPromotions(@Param("search") String search,
                                      @Param("fromDate") LocalDateTime fromDate,
                                      @Param("toDate") LocalDateTime toDate,
                                      @Param("status") Byte status);
    @Query("SELECT p FROM Promotion p WHERE p.id = :id1")
    Promotion findeById(@Param("id1") Integer id);
}
