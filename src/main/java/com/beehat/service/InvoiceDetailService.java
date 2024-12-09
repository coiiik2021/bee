package com.beehat.service;


import com.beehat.DTO.ProductResponse;
import com.beehat.entity.InvoiceDetail;
import com.beehat.entity.Product;
import com.beehat.entity.ProductDetail;
import com.beehat.repository.InvoiceDetailRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InvoiceDetailService {
    @Autowired
    InvoiceDetailRepo invoiceDetailRepo;

    public List<ProductResponse> countSL() {
        List<InvoiceDetail> invoiceDetailList = invoiceDetailRepo.findAll();
        Map<Product, Integer> result = new HashMap<>();

        for (InvoiceDetail invoiceDetail : invoiceDetailList) {
            ProductDetail productDetail = invoiceDetail.getProductDetail();
            int quantity = invoiceDetail.getQuantity();
            System.out.println(productDetail);

            if (productDetail != null && result.containsKey(productDetail.getProduct())) {
                result.put(productDetail.getProduct(), result.get(productDetail.getProduct()) + quantity);
            } else {
                result.put(productDetail.getProduct(), quantity);
            }
        }

        List<ProductResponse> productResponseList = new ArrayList<>();

        for (Map.Entry<Product, Integer> entry : result.entrySet()) {
            ProductResponse productResponse = ProductResponse.builder()
                    .image(entry.getKey().getImages().getFirst().getImageUrl())
                    .sl(entry.getValue())
                    .price(entry.getValue() * entry.getKey().getProductDetail().getFirst().getPrice())
                    .name(entry.getKey().getName())
                    .build();
            productResponseList.add(productResponse);
        }

        productResponseList.sort((p1, p2) -> Integer.compare(p2.getSl(), p1.getSl()));

        return productResponseList;
    }


}
