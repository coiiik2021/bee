package com.beehat.repository;

import com.beehat.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SizeRepo extends JpaRepository<Size, Integer> {
    List<Size> findByStatus(Byte status);
}
