package com.beehat.service;

import com.beehat.entity.*;
import com.beehat.repository.CartDetailRepo;
import com.beehat.repository.ProductDetailRepo;
import com.beehat.repository.ProductRepo;
import com.beehat.repository.VoucherRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@SessionScope
public class CartService {
    @Autowired
    ProductDetailRepo productDetailRepo;
    @Autowired
    CartDetailRepo cartDetailRepo;
    @Autowired
    VoucherRepo voucherRepo;
    private List<CartDetail> cartDetails = new ArrayList<>();
    private Invoice temporaryInvoice;

    public List<CartDetail> getCartDetails() {
        return cartDetails;
    }

    public Invoice getTemporaryInvoice() {
        return temporaryInvoice;
    }
    public void addAllCart(List<CartDetail> newCartDetails) {
        cartDetails.clear();
        this.cartDetails = newCartDetails;
    }
    public void createTemporaryInvoice(Customer customer,List<CartDetail> carts) {
        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setInvoiceStatus((byte) 1);
        invoice.setTotalPrice(carts.stream()
                .mapToInt(cart -> cart.getQuantity() * cart.getProductDetail().getPrice())
                .sum());
        invoice.setFinalPrice(invoice.getTotalPrice());
        this.temporaryInvoice = invoice;
    }



    public void clearTemporaryInvoice() {
        this.temporaryInvoice = null;
        this.cartDetails.clear();
    }
    public void add(int id,int quantity) {
        CartDetail cartDetail = cartDetails.
                stream()
                .filter(prd -> prd.getProductDetail().getId() == id)
                .findFirst().orElse(null);
        if (cartDetail != null) {
            cartDetail.setQuantity(cartDetail.getQuantity() + quantity);
            return;
        }
        ProductDetail productDetail = productDetailRepo.findById(id).orElse(null);
        if (productDetail != null) {
            cartDetails.add(
                    new CartDetail(null, productDetail, null, quantity, null, null, null, null, null, null, null)
            );
        }
    }

    public void remove(int id) {
        cartDetails = cartDetails
                .stream()
                .filter(it -> it.getProductDetail().getId() != id)
                .collect(Collectors.toList());
    }

    public void clear() {
        cartDetails.clear();
    }

    public void update(int id, int qty) {
        CartDetail item = cartDetails
                .stream()
                .filter(it -> it.getProductDetail().getId() == id)
                .findFirst()
                .orElse(null);

        if (item != null) {
            item.setQuantity(qty);
        }
    }

    public long getTotal() {
        long total = cartDetails.stream()
                .map(CartDetail::getProductDetail)  // Lấy ra đối tượng ProductDetail từ mỗi CartDetail
                .distinct()  // Loại bỏ sản phẩm trùng lặp (nếu cần)
                .count();  // Đếm số lượng các sản phẩm
        return total;
    }

    public int getAmount() {
        int amount = 0;
        for (CartDetail item : cartDetails) {
            amount += item.getQuantity() * item.getProductDetail().getPrice();
        }
        return amount;
    }

    public void mergeCartWithUserCart(Customer customer) {
        // Giỏ hàng tạm thời trong session
        List<CartDetail> tempCart = getCartDetails();
        // Nếu không có sản phẩm nào trong giỏ hàng tạm thời, không cần gộp
        if (tempCart == null || tempCart.isEmpty()) {
            return;
        }
        // Giỏ hàng của người dùng đã đăng nhập (từ cơ sở dữ liệu)
        List<CartDetail> userCart = cartDetailRepo.findByCustomerIdAndStatus(customer.getId(), (byte) 1);

        // Gộp giỏ hàng: nếu sản phẩm đã có trong giỏ hàng người dùng, cộng số lượng lại
        for (CartDetail tempCartDetail : tempCart) {
            boolean found = false;
            for (CartDetail userCartDetail : userCart) {
                if (userCartDetail.getProductDetail().getId().equals(tempCartDetail.getProductDetail().getId())) {
                    // Nếu sản phẩm đã có trong giỏ hàng người dùng, cộng số lượng
                    userCartDetail.setQuantity(userCartDetail.getQuantity() + tempCartDetail.getQuantity());
                    found = true;
                    break;
                }
            }

            // Nếu sản phẩm chưa có trong giỏ hàng người dùng, thêm mới vào giỏ hàng người dùng
            if (!found) {
                userCart.add(tempCartDetail);
            }
        }

        // Cập nhật giỏ hàng của người dùng vào cơ sở dữ liệu
        for (CartDetail cartDetail : userCart) {
            cartDetail.setCustomer(customer);  // Đảm bảo gán đúng người dùng
            cartDetailRepo.save(cartDetail);
        }

        // Xóa giỏ hàng tạm thời trong session (nếu cần)
        clear();
    }
}
