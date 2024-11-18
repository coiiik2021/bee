package com.beehat.repository;

import com.beehat.entity.CartDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartDetailRepo extends JpaRepository<CartDetail, Integer> {
    List<CartDetail> findByCustomerId(int customerId);
}