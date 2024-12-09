package com.beehat.repository;

import com.beehat.entity.InvoiceDetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface InvoiceDetailRepo extends JpaRepository<InvoiceDetail, Integer> {
    List<InvoiceDetail> findByInvoiceId(Integer invoiceId);
    List<InvoiceDetail> findByProductDetailId(Integer productDetailId);

    @Query("SELECT pd.id AS productId, SUM(id.quantity) AS totalQuantity " +
            "FROM InvoiceDetail id " +
            "JOIN id.productDetail pd " +
            "GROUP BY pd.id " +
            "ORDER BY totalQuantity DESC")
    List<Map<String, Object>> findTopSellingProducts(Pageable pageable);
}
