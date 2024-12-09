package com.beehat.repository;

import com.beehat.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepo extends JpaRepository<Material, Integer> {
    List<Material> findByStatus(Byte status);
}
