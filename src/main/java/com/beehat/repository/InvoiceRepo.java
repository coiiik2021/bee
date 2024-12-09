package com.beehat.repository;

import com.beehat.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepo extends JpaRepository<Invoice, Integer> {
    List<Invoice> findByStatusAndInvoiceStatus(Byte status, Byte invoiceStatus);
    List<Invoice> findByCustomerId(Integer id);


}
