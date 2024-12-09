package com.beehat.repository;

import com.beehat.entity.Customer;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomerRepo extends JpaRepository<Customer, Integer> {
    @Query("SELECT e FROM Customer e WHERE e.id = :id1")
    Customer findeById(@Param("id1") Integer id);
    @Transactional
    @Query("SELECT e FROM Customer e WHERE e.username = :username")
    Customer findByUsername(@Param("username") String username);
    @Query("SELECT e FROM Customer e WHERE e.email = :email")
    Customer findByEmail(@Param("email") String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    @Query("SELECT c FROM Customer c " +
            "WHERE (:searchValue IS NULL OR c.username LIKE %:searchValue% OR c.fullname LIKE %:searchValue% OR c.phone LIKE %:searchValue%) " +
            "AND (:status IS NULL OR c.status = :status) " +
            "AND (:fromDate IS NULL OR c.createdDate >= :fromDate) " +
            "AND (:toDate IS NULL OR c.createdDate <= :toDate)")
    List<Customer> searchCustomers(@Param("searchValue") String searchValue,
                                   @Param("status") Byte status,
                                   @Param("fromDate") LocalDateTime fromDate,
                                   @Param("toDate") LocalDateTime toDate);
    List<Customer> findByStatus(Byte status);
}
