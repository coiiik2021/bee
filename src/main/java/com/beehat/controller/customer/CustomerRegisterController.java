package com.beehat.controller.customer;

import com.beehat.entity.Customer;
import com.beehat.entity.Product;
import com.beehat.repository.CustomerRepo;
import com.beehat.repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Controller
@RequestMapping("/customer")
public class CustomerRegisterController {
    @Autowired
    CustomerRepo customerRepo;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private JavaMailSender mailSender;
    private String verificationCode;
    @GetMapping("/index")
    public String index(Authentication authentication, Model model){
        List<Product> products = productRepo.findAll();
        model.addAttribute("products",products);
        // Kiểm tra xem người dùng đã đăng nhập hay chưa
        if (authentication != null && authentication.isAuthenticated()) {
            // Lấy thông tin người dùng từ SecurityContext
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            System.out.println(username);
            model.addAttribute("username1", username); // Thêm tên người dùng vào mô hình
        }
        return "customer/index";
    }
    @GetMapping("/login")
    public String showLoginPage(Authentication authentication,Model model) {
        model.addAttribute("customer", new Customer());
        // Kiểm tra xem người dùng đã đăng nhập hay chưa
        if (authentication != null && authentication.isAuthenticated()) {

            return "redirect:/";
        }
        return "customer/customerLogin"; // Trả về tên template đăng nhập
    }
    @GetMapping("/register")
    public String showLoginPage(Model model) {
        model.addAttribute("customer", new Customer());
        // Kiểm tra xem người dùng đã đăng nhập hay chưa

        return "customer/register"; // Trả về tên template đăng nhập
    }
    @PostMapping("/register")
    public String registerCustomer(@ModelAttribute("customer") Customer customer,
                                   BindingResult result,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        boolean usernameExists = customerRepo.existsByUsername(customer.getUsername());
        boolean emailExists = customerRepo.existsByEmail(customer.getEmail());

        if (usernameExists) {
            result.rejectValue("username", "error.customer", "Username already exists !");
        }

        if (emailExists) {
            result.rejectValue("email", "error.customer", "Email already exists !");
        }

        if (result.hasErrors()) {
            return "customer/register";
        }

        customer.setCreatedDate(LocalDateTime.now());
        customer.setStatus(Byte.valueOf("1"));
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        customerRepo.save(customer);
        redirectAttributes.addFlashAttribute("successMessage", "Register successfully! Please log in.");

        return "redirect:/customer/login";
    }
    @GetMapping("/forgot-password")
    public String forgotPass(){
        return "/customer/forgot-password";
    }
    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam String email, Model model) {
        Optional<Customer> customer = Optional.ofNullable(customerRepo.findByEmail(email));
        if (!customer.isPresent()) {
            model.addAttribute("error", "Email does not exist, please try again !.");
            return "/customer/forgot-password";  // Trả về trang cùng thông báo lỗi
        }
        System.out.println("email forgot:"+email);
        verificationCode = generateVerificationCode();
        sendEmail(email, "Password Reset Verification Code", "Your verification code is: " + verificationCode);

        model.addAttribute("message", "Verification code sent to your email.");
        model.addAttribute("email", email);
        return "/customer/verify-code";  // Chuyển hướng đến trang nhập mã xác thực
    }
    @PostMapping("/verify-code")
    public String verifyCode(@RequestParam String code, @RequestParam String email, Model model) {
        System.out.println("email verify:"+email);
        if (verificationCode.equals(code)) {
            model.addAttribute("email", email);
            return "/customer/reset-password";  // Nếu mã đúng, chuyển đến trang đặt lại mật khẩu
        } else {
            model.addAttribute("error", "Invalid verification code.");
            return "/customer/verify-code";  // Nếu mã sai, giữ nguyên trang mã xác nhận với thông báo lỗi
        }
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email, @RequestParam String newPassword,
                                @RequestParam String confirmPassword, Model model, RedirectAttributes redirectAttributes) {
        System.out.println("email reset:"+email);
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Confirmation password does not match.");
            model.addAttribute("email", email);
            return "/customer/reset-password";
        }
        model.addAttribute("email", email);
        Optional<Customer> customer1 = Optional.ofNullable(customerRepo.findByEmail(email));

        if (customer1.isPresent()) {
            Customer customer = customer1.get();
            customer.setPassword(passwordEncoder.encode(newPassword));  // Cập nhật mật khẩu mới (có thể thêm mã hóa)
            customerRepo.save(customer);
            redirectAttributes.addFlashAttribute("successMessage", "Password recovery successful! Please log in.");
            return "redirect:/customer/login";
        }
        else {
            model.addAttribute("error", "User not found.");
            return "/customer/reset-password";
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
