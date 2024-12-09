package com.beehat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "invoice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)
    private List<InvoiceDetail> invoiceDetails = new ArrayList<>();

    @Column(name = "deliver_date")
    private LocalDateTime deliverDate;

    @Column(name = "total_price")
    private Integer totalPrice;

    @ManyToOne
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    @Column(name = "voucher_discount")
    private Integer voucherDiscount;

    @Column(name = "final_price")
    private Integer finalPrice;

    @Column(name = "shipping_address")
    private String shippingAddress;

    @Column(name = "shipping_city")
    private String shippingCity;

    @Column(name = "shipping_district")
    private String shippingDistrict;

    @Column(name = "shipping_ward")
    private String shippingWard;

    @Column(name = "shipping_country")
    private String shippingCountry;

    @Column(name = "phone")
    private String phone;

    @Column(name = "invoice_tracking_number")
    private String invoiceTrackingNumber; // Số theo dõi đơn hàng

    @Column(name = "invoice_status")
    private Byte invoiceStatus; // Trạng thái đơn hàng

    @Column(name = "status", insertable = false)
    private Byte status;
    // Relationships with history tables
    @OneToMany(mappedBy = "invoice")
    private List<InvoiceStatusHistory> statusHistories;

    @OneToMany(mappedBy = "invoice")
    private List<PaymentHistory> paymentHistories;
    @Column(name = "created_date", insertable = false, updatable = false)
    private LocalDateTime createdDate;
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    public void prePersist() {
        // Nếu SKU chưa có, tự động tạo mới SKU
        if (this.invoiceTrackingNumber == null || this.invoiceTrackingNumber.isEmpty()) {
            this.invoiceTrackingNumber = generateInvoiceTrackingNumber();
        }
    }

    // Phương thức sinh mã theo dõi hoá đơn
    private String generateInvoiceTrackingNumber() {
        // Sinh chuỗi ngẫu nhiên 5 ký tự
        String randomUUID = UUID.randomUUID().toString().substring(0, 5).toUpperCase();

        // Thời gian hiện tại dưới dạng yyyyMMddHHmmss
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMddHHmmss"));

        // Tạo SKU bằng cách kết hợp productCode, timeStamp, và randomUUID
        return randomUUID + "-" + timeStamp;
    }

    @PreUpdate
    public void preUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
