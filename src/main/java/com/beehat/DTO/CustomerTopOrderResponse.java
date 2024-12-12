package com.beehat.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CustomerTopOrderResponse {
    private String nameCustomer;
    private String phoneNumber;
    private int totalOrders;
    private Long totalPayments;

}
