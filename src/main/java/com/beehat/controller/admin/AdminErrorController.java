package com.beehat.controller.admin;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
@Controller
public class AdminErrorController {
    @RequestMapping("/employeeerror")
    public String handleError(HttpServletRequest request, Model model) {
        // Lấy status code của lỗi
        Object status = request.getAttribute("javax.servlet.error.status_code");

        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());

            if (statusCode == 403) {
                return "error/403";  // Trả về trang 403
            } else if (statusCode == 404) {
                return "error/404";  // Trả về trang 404
            } else if (statusCode == 500) {
                return "error/500";  // Trả về trang 500
            }
        }

        // Trả về trang lỗi chung nếu không tìm thấy lỗi cụ thể
        return "error/error";
    }
    @GetMapping("/switch")
    public String switchSecurityContext(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && (auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))
                || auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_EMPLOYEE")))) {

            // Nếu người dùng có role là ADMIN hoặc EMPLOYEE, thực hiện đăng xuất
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/"; // Chuyển hướng sang trang login của khu vực khác
    }
}
