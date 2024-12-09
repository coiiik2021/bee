package com.beehat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String username;

    private String password;

    private String fullname;

    private String email;

    private String address;

    private String city;

    private String district;

    private String ward;

    private String country;

    private String photo;

    private String phone;
    @Column(insertable = false)
    private Byte status;

    @Column(name = "created_date", insertable = false, updatable = false)
    private LocalDateTime createdDate;
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    // Các trường tạm thời để lưu tên của tỉnh, huyện, xã (Không cần lưu vào cơ sở dữ liệu)
    @Transient
    private String provinceName;

    @Transient
    private String districtName;

    @Transient
    private String wardName;
    @PreUpdate
    public void preUpdate() {
        updatedDate = LocalDateTime.now();
    }
    public Customer( String username, String password, String fullname, String email, String address, String city, String district, String ward, String country, String photo, String phone, Byte status) {
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.email = email;
        this.address = address;
        this.city = city;
        this.district = district;
        this.ward = ward;
        this.country = country;
        this.photo = photo;
        this.phone = phone;
        this.status = status;
    }
}

