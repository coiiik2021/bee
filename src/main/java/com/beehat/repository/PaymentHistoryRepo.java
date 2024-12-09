package com.beehat.repository;

import com.beehat.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentHistoryRepo extends JpaRepository<PaymentHistory, Integer> {
    List<PaymentHistory> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
