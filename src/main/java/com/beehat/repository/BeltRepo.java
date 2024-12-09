package com.beehat.repository;

import com.beehat.entity.Belt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeltRepo extends JpaRepository<Belt, Integer> {
    List<Belt> findByStatus(Byte status);
}
