package com.beehat.security;

import com.beehat.entity.Customer;
import com.beehat.repository.CartDetailRepo;
import com.beehat.repository.CustomerRepo;
import com.beehat.service.CartService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
public class CustomAuthenticationSuccessHandlerForCustomer implements AuthenticationSuccessHandler {
    private final CartService cartService;
    private final CustomerRepo customerRepo;
    private final CartDetailRepo cartDetailRepo;

    // Constructor injection để sử dụng CartService
    public CustomAuthenticationSuccessHandlerForCustomer(CartService cartService,CustomerRepo customerRepo,CartDetailRepo cartDetailRepo) {
        this.cartService = cartService;
        this.customerRepo = customerRepo;
        this.cartDetailRepo = cartDetailRepo;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // Lấy username của người dùng hiện tại
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();

        // Tìm Customer trong cơ sở dữ liệu dựa trên username
        Customer customer = customerRepo.findByUsername(username);
        // Thực hiện gộp giỏ hàng cho Customer
        cartService.mergeCartWithUserCart(customer);
        // Điều hướng về trang chủ hoặc trang giỏ hàng sau khi đăng nhập thành công
        response.sendRedirect("/");
    }
}
