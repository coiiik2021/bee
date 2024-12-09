package com.beehat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "invoice_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @ManyToOne
    @JoinColumn(name = "product_detail_id")
    private ProductDetail productDetail;

    private Integer quantity;

    @Column(name = "unit_price")
    private Integer unitPrice;

    @Column(name = "discount_percentage")
    private Byte discountPercentage; // Phần trăm khuyến mãi (%)

    @Column(name = "discount_amount")
    private Integer discountAmount; // Giá trị giảm giá trực tiếp

    @Column(name = "final_price")
    private Integer finalPrice; // Giá sau khi giảm giá

    @Column(insertable = false)
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
