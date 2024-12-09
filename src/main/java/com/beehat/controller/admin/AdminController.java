package com.beehat.controller.admin;

import com.beehat.entity.Employee;
import com.beehat.repository.EmployeeRepo;
import com.beehat.service.DashBoard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    EmployeeRepo employeeRepo;
    @Autowired
    DashBoard dashBoard;


    @ModelAttribute("iconTitle")
    String iconTitle() {
        return "ph ph-house-simple fs-3";
    }
    @ModelAttribute("pageTitle")
    String pageTitle() {
        return "Trang chủ";
    }


    @GetMapping("/index")
    public String index(Model model) {
        int[] total = dashBoard.total();
        int[] online = dashBoard.totalOnline();

        int[] offline = new int[]{Math.abs(total[0] - online[0]), Math.abs(total[1] - online[1])};


        double[] tile = dashBoard.tileOnline();


        model.addAttribute("total", total);
        model.addAttribute("online", online);
        model.addAttribute("offline", offline);
        model.addAttribute("tile", tile);

        return "admin/index";
    }
    @GetMapping("/login")
    public String showLoginPage(Principal principal) {
        // Lấy Authentication từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Kiểm tra xem người dùng đã đăng nhập hay chưa
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            // Lấy thông tin từ UserDetails
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            Employee employee = employeeRepo.findByUsername(username);
            //Nếu đã đăng nhập, chuyển về trang theo role
            if (employee != null) {
                if (employee.getRole() == 1) {
                    return "redirect:/admin/index";
                } else if (employee.getRole() == 0) {
                    return "redirect:/employee/dashboard";
                }
            }
        }
        return "admin/loginadmin";
    }

}
