package com.beehat.repository;

import com.beehat.entity.InvoiceStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceHistoryStatusRepo extends JpaRepository<InvoiceStatusHistory, Integer> {
    InvoiceStatusHistory findByInvoiceId(int invoiceId);
    InvoiceStatusHistory findByInvoiceIdAndNewStatus(int invoiceId,byte status);
}
