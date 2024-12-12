package com.beehat.service;


import com.beehat.DTO.CustomerTopOrderResponse;
import com.beehat.entity.Customer;
import com.beehat.entity.Invoice;
import com.beehat.repository.CustomerRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerService {
    CustomerRepo customerRepo;

    public List<CustomerTopOrderResponse> getListCustomerTopOrder() {

        List<Customer> customers = customerRepo.findAll();
        List<CustomerTopOrderResponse> topCustomers = new ArrayList<>();
        for (Customer customer : customers) {

            if(!customer.getInvoices().isEmpty()){
                int check = customer.getInvoices().stream().filter(
                        invoice -> invoice.getStatus() == 8
                ).toArray().length;
                if(check != 0){
                    topCustomers.add(
                            CustomerTopOrderResponse.builder()
                                    .nameCustomer(customer.getFullname())
                                    .phoneNumber(customer.getPhone())
                                    .totalOrders(check)
                                    .totalPayments(
                                            customer.getInvoices().stream().filter(
                                                    invoice -> invoice.getStatus() == 8
                                            ).mapToLong(Invoice::getTotalPrice).sum()
                                    )
                                    .build()
                    );


                }


            }

        }

        topCustomers.sort((c1, c2) -> Long.compare(c2.getTotalPayments(), c1.getTotalPayments()));
        for (CustomerTopOrderResponse customerTopOrderResponse : topCustomers) {
            System.out.println(customerTopOrderResponse);
        }

        return topCustomers;

    }
}
