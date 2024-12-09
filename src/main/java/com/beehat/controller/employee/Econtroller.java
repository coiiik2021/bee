package com.beehat.controller.employee;

import com.beehat.entity.Employee;
import com.beehat.repository.EmployeeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/employee")
public class Econtroller {
    @Autowired
    EmployeeRepo employeeRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @ModelAttribute("iconTitle")
    String iconTitle() {
        return "ph ph-house-simple fs-3";
    }
    @ModelAttribute("pageTitle")
    String pageTitle() {
        return "Trang chủ";
    }
    @GetMapping("/dashboard")
    public String getdashboard(){
        return "/employee/dashboard";
    }
    @GetMapping("/profile")
    public String showProfile(Principal principal, Model model) {
        // Lấy thông tin từ Authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            // Lấy thông tin từ UserDetails
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();

            // Lấy Employee từ repository
            Employee employee = employeeRepo.findByUsername(username);
            model.addAttribute("employee",employee);
        }

        return "employee/profile"; // Trả về trang hiển thị thông tin
    }
    @PostMapping("/update")
    public String updateEmployee(@ModelAttribute Employee employee) {
        employee.setUpdatedDate(LocalDateTime.now());

        employeeRepo.save(employee);
        return "redirect:/employee/dashboard";
    }
    @GetMapping("/change-password")
    public String changepass(){
        return "/employee/changepassword";
    }
    @PostMapping("/changepassword")
    public String changepassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes){
        String username = principal.getName();
        Employee employee = employeeRepo.findByUsername(username);
        System.out.println("pass:"+currentPassword+"passnew:"+newPassword+":"+confirmPassword+":"+username);
        if(!passwordEncoder.matches(currentPassword,employee.getPassword())){
            redirectAttributes.addFlashAttribute("error","Mật khẩu hiện tại không chính xác !");
            return "redirect:/employee/change-password";
        }
        if(!newPassword.equals(confirmPassword)){
            redirectAttributes.addFlashAttribute("error","Mật khẩu xác nhận không khớp với mật khẩu mới !");
            return "redirect:/employee/change-password";
        }
        employee.setPassword(passwordEncoder.encode(newPassword));
        employeeRepo.save(employee);
        redirectAttributes.addFlashAttribute("message","Thay đổi thành công !");
        return "redirect:/employee/dashboard";
    }
}
