package com.beehat.controller.admin;

import com.beehat.entity.Employee;
import com.beehat.repository.EmployeeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.Random;

@Controller
@RequestMapping("/auth")
public class PasswordController {
    @Autowired
    EmployeeRepo employeeRepo;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    PasswordEncoder passwordEncoder;
    private String verificationCode;

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "/admin/forgot-password";  // Hiển thị trang quên mật khẩu
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam String email, Model model) {
        Optional<Employee> employee = Optional.ofNullable(employeeRepo.findByEmail(email));
        System.out.println("email forgot:"+email);
        if (!employee.isPresent()) {
            model.addAttribute("error", "Email không tồn tại trong hệ thống.");
            return "/admin/forgot-password";  // Trả về trang cùng thông báo lỗi
        }

        verificationCode = generateVerificationCode();
        sendEmail(email, "Password Reset Verification Code", "Your verification code is: " + verificationCode);

        model.addAttribute("message", "Verification code sent to your email.");
        model.addAttribute("email", email);
        return "/admin/verify-code";  // Chuyển hướng đến trang nhập mã xác thực
    }

    @PostMapping("/verify-code")
    public String verifyCode(@RequestParam String code, @RequestParam String email, Model model) {
        System.out.println("email verify:"+email);
        if (verificationCode.equals(code)) {
            model.addAttribute("email", email);
            return "/admin/reset-password";  // Nếu mã đúng, chuyển đến trang đặt lại mật khẩu
        } else {
            model.addAttribute("error", "Invalid verification code.");
            return "/admin/verify-code";  // Nếu mã sai, giữ nguyên trang mã xác nhận với thông báo lỗi
        }
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email, @RequestParam String newPassword,
                                @RequestParam String confirmPassword, Model model, RedirectAttributes redirectAttributes) {
        System.out.println("email reset:"+email);
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không trùng khớp.");
            model.addAttribute("email", email);
            return "/admin/reset-password";
        }

        Optional<Employee> employee1 = Optional.ofNullable(employeeRepo.findByEmail(email));

        if (employee1.isPresent()) {
            Employee employee = employee1.get();
            employee.setPassword(passwordEncoder.encode(newPassword));  // Cập nhật mật khẩu mới (có thể thêm mã hóa)
            employeeRepo.save(employee);
            redirectAttributes.addFlashAttribute("successMessage", "Khôi phuc mật khẩu thành công! Vui lòng đăng nhập.");
            return "redirect:/admin/login";
        }
        else {
            model.addAttribute("error", "User not found.");
            return "/admin/reset-password";
        }
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
