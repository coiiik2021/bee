package com.beehat.service;

import com.beehat.entity.*;
import com.beehat.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PromotionService {
//    private static final Logger logger = LoggerFactory.getLogger(PromotionService.class);
    @Autowired
    PromotionRepo promotionRepo;
    @Autowired
    private ProductPromotionRepo productPromotionRepo;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ProductDetailRepo productDetailRepo;
    @Autowired
    private InvoiceDetailRepo invoiceDetailRepo;
    @Autowired
    private InvoiceRepo invoiceRepo;
    @Transactional
    @Scheduled(cron = "0 * * * * ?")
    public void updatePromotionStatus() {
        System.out.println("Updating promotion statuses...");
        LocalDateTime today = LocalDateTime.now();
        List<Promotion> promotions = promotionRepo.findAll();

        for (Promotion promotion : promotions) {
            if (promotion.getStartDate().isAfter(today)) {
                promotion.setStatus((byte) 0); // Chưa bắt đầu
            } else if (promotion.getEndDate().isBefore(today)) {
                resetInvoiceDetailsToOriginalPrice(promotion);
                promotion.setStatus((byte) 2); // Đã kết thúc
                productRepo.clearPromotionByPromotionId(promotion.getId());
            } else {
                promotion.setStatus((byte) 1); // Đang diễn ra
                applyDiscountedPrice(promotion); // Áp dụng giá giảm khi chương trình bắt đầu
                updateProductPromotion(promotion);
            }
        }
        promotionRepo.saveAll(promotions);
    }
    @Scheduled(cron = "0 * * * * ?") // Cài đặt để chạy hàm mỗi giờ
    protected void resetALLInvoiceDetailPrice() {
        System.out.println("Cập nhật giá sau mỗi phút ....");
        for (Invoice invoice : invoiceRepo.findByStatusAndInvoiceStatus((byte) 0,(byte) 0)) {
            List<InvoiceDetail> allInvoiceDetails = invoiceDetailRepo.findByInvoiceId(invoice.getId());

            for (InvoiceDetail invoiceDetail : allInvoiceDetails) {
                // Lấy giá sản phẩm từ DB và áp dụng lại nếu có khuyến mãi
                ProductDetail productDetail = invoiceDetail.getProductDetail();
                int currentPrice = productDetail.getPrice();
                Promotion promotion = productDetail.getProduct().getPromotion();

                if (promotion != null && promotion.getStatus() == 1) { // Áp dụng giá khuyến mãi nếu đang diễn ra
                    currentPrice = calculateDiscountedPrice(productDetail, promotion);
                }

                // Cập nhật giá của chi tiết hóa đơn
                invoiceDetail.setUnitPrice(currentPrice);
                invoiceDetail.setFinalPrice(currentPrice * invoiceDetail.getQuantity());
                invoiceDetailRepo.save(invoiceDetail);
            }

            // Cập nhật tổng tiền của hóa đơn
            int totalPrice = allInvoiceDetails.stream()
                    .mapToInt(detail -> detail.getProductDetail().getPrice() * detail.getQuantity())
                    .sum();
            int finalInvoicePrice = allInvoiceDetails.stream()
                    .mapToInt(InvoiceDetail::getFinalPrice)
                    .sum();
            int discountMoney = totalPrice - finalInvoicePrice;

            invoice.setVoucherDiscount(discountMoney);
            invoice.setTotalPrice(totalPrice);
            invoice.setFinalPrice(finalInvoicePrice);
            invoiceRepo.save(invoice);

        }
    }
    public List<Promotion> getAllPromotions() {
        return promotionRepo.findAll();
    }
    // Đặt lại giá gốc cho tất cả các sản phẩm trong hóa đơn sau khi chương trình khuyến mãi kết thúc
    private void resetInvoiceDetailsToOriginalPrice(Promotion promotion) {
        List<ProductPromotion> promotionProducts = productPromotionRepo.findAllByPromotionAndStatus(promotion, (byte) 1);
        for (ProductPromotion productPromotion : promotionProducts) {
            List<ProductDetail> productDetails = productDetailRepo.findByProductId(productPromotion.getProduct().getId());

            for (ProductDetail productDetail : productDetails) {
                List<InvoiceDetail> invoiceDetails = invoiceDetailRepo.findByProductDetailId(productDetail.getId());
                for (InvoiceDetail invoiceDetail : invoiceDetails) {
                    Invoice invoice = invoiceRepo.findById(invoiceDetail.getInvoice().getId()).orElse(null);
                    // Chỉ cập nhật nếu hóa đơn đang chờ thanh toán
                    if (invoice != null && invoice.getStatus() == 0) {
                        resetInvoiceDetailPrice(invoiceDetail);
                    }
                }
            }
        }
    }

    // Cập nhật lại giá gốc cho một hóa đơn chi tiết cụ thể
    private void resetInvoiceDetailPrice(InvoiceDetail invoiceDetail) {
        Invoice invoice = invoiceRepo.findById(invoiceDetail.getInvoice().getId()).orElse(null);
        if (invoice != null && invoice.getStatus() == (byte) 0) {
            invoiceDetail.setUnitPrice(invoiceDetail.getProductDetail().getPrice());
            invoiceDetail.setFinalPrice(invoiceDetail.getProductDetail().getPrice() * invoiceDetail.getQuantity());
            invoiceDetailRepo.save(invoiceDetail);

            // Cập nhật tổng tiền của hóa đơn
            List<InvoiceDetail> allInvoiceDetails = invoiceDetailRepo.findByInvoiceId(invoice.getId());
            // Cập nhật tổng tiền của hóa đơn
            int totalPrice = allInvoiceDetails.stream()
                    .mapToInt(detail -> detail.getProductDetail().getPrice() * detail.getQuantity())
                    .sum();
            int finalInvoicePrice = allInvoiceDetails.stream()
                    .mapToInt(InvoiceDetail::getFinalPrice)
                    .sum();
            int discountMoney = totalPrice - finalInvoicePrice;

            invoice.setVoucherDiscount(discountMoney);
            invoice.setTotalPrice(totalPrice);
            invoice.setFinalPrice(finalInvoicePrice);
            invoiceRepo.save(invoice);
        }
    }

    // Thêm hoặc cập nhật các sản phẩm liên kết với promotion trong PromotionProduct
    private void updateProductPromotion(Promotion promotion) {
        List<ProductPromotion> promotionProducts = productPromotionRepo.findAllByPromotionAndStatus(promotion, (byte) 1);
        for (ProductPromotion promotionProduct : promotionProducts) {
            Product product = promotionProduct.getProduct();
            product.setPromotion(promotion); // Cập nhật khuyến mãi hiện tại cho sản phẩm
            productRepo.save(product);
        }
    }
    // Áp dụng giá giảm cho sản phẩm khi chương trình khuyến mãi bắt đầu
    private void applyDiscountedPrice(Promotion promotion) {
        List<ProductPromotion> promotionProducts = productPromotionRepo.findAllByPromotionAndStatus(promotion, (byte) 1);

        for (ProductPromotion productPromotion : promotionProducts) {
            Product product = productPromotion.getProduct();
            List<ProductDetail> productDetails = productDetailRepo.findByProductId(product.getId());

            for (ProductDetail productDetail : productDetails) {
                int discountedPrice = calculateDiscountedPrice(productDetail, promotion);

                // Cập nhật giá trong các hóa đơn chi tiết nếu sản phẩm này đã có trong hóa đơn
                List<InvoiceDetail> invoiceDetails = invoiceDetailRepo.findByProductDetailId(productDetail.getId());
                for (InvoiceDetail invoiceDetail : invoiceDetails) {
                    Invoice invoice = invoiceDetail.getInvoice();
                    // Chỉ cập nhật nếu hóa đơn đang chờ thanh toán
                    if (invoice.getStatus() == 0) {
                        invoiceDetail.setUnitPrice(discountedPrice);
                        invoiceDetail.setFinalPrice(discountedPrice * invoiceDetail.getQuantity());
                        invoiceDetailRepo.save(invoiceDetail);

                        // Cập nhật lại tổng giá cho hóa đơn
                        List<InvoiceDetail> allInvoiceDetails = invoiceDetailRepo.findByInvoiceId(invoice.getId());
                        // Cập nhật tổng tiền của hóa đơn
                        int totalPrice = allInvoiceDetails.stream()
                                .mapToInt(detail -> detail.getProductDetail().getPrice() * detail.getQuantity())
                                .sum();
                        int finalInvoicePrice = allInvoiceDetails.stream()
                                .mapToInt(InvoiceDetail::getFinalPrice)
                                .sum();
                        int discountMoney = totalPrice - finalInvoicePrice;

                        invoice.setVoucherDiscount(discountMoney);
                        invoice.setTotalPrice(totalPrice);
                        invoice.setFinalPrice(finalInvoicePrice);
                        invoiceRepo.save(invoice);

                    }
                }
            }
        }
    }

    // Tính giá sau khi giảm
    private int calculateDiscountedPrice(ProductDetail productDetail, Promotion promotion) {
        if (promotion.getDiscountAmount() != null && promotion.getDiscountAmount() > 0) {
            return productDetail.getPrice() - promotion.getDiscountAmount();
        } else if (promotion.getDiscountPercentage() != null) {
            return productDetail.getPrice() - (productDetail.getPrice() * promotion.getDiscountPercentage()) / 100;
        }
        return productDetail.getPrice();
    }

}
