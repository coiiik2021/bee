package com.beehat.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lining")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Lining {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    // Validate name to not be null, with length between 1 and 50 characters
    @NotBlank(message = "Tên không được bỏ trống !")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "status", nullable = false, insertable = false)
    private Byte status;

    @Column(name = "created_date", insertable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PreUpdate
    public void preUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
