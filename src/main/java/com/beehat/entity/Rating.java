package com.beehat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rating")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne @JoinColumn(name = "customer_id")
    private Customer customer; // Khóa ngoại đến bảng customer (có thể null)

    private Integer rating; // Đánh giá từ 1-5

    private String comment; // Bình luận của người dùng

    private Byte status; // Trạng thái (TINYINT)

    @Column(name = "created_date", insertable = false, updatable = false)
    private LocalDateTime createdDate;
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    @PreUpdate
    public void preUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
