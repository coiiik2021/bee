package com.beehat.repository;

import com.beehat.entity.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColorRepo extends JpaRepository<Color, Integer> {
    List<Color> findByStatus(Byte status);
}
