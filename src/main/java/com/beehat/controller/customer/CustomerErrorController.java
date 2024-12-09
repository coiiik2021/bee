package com.beehat.controller.customer;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomerErrorController {
    @RequestMapping("/customererror")
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
}
