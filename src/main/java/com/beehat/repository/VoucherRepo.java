package com.beehat.repository;

import com.beehat.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoucherRepo extends JpaRepository<Voucher, Integer> {
    List<Voucher> findByStatus(byte status);
    Voucher findByCode(String code);
}
