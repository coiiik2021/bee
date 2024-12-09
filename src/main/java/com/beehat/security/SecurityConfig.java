package com.beehat.security;

import com.beehat.entity.Customer;
import com.beehat.entity.Employee;
import com.beehat.repository.CustomerRepo;
import com.beehat.repository.EmployeeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    // Encoder chung cho các cấu hình
    @Bean
    public static PasswordEncoder encoder() {
        return new BCryptPasswordEncoder(10);
    }
    @Autowired
    private CustomerRepo customerRepo;
    @Autowired
    private EmployeeRepo employeeRepo;
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            Employee employee = employeeRepo.findByUsername(username);
            if (employee != null) {
                // trả về đối tượng User cho Employee
                return org.springframework.security.core.userdetails.User
                        .withUsername(employee.getUsername())
                        .password(employee.getPassword())
                        .authorities(employee.getRole() == 1 ? "ROLE_ADMIN" : "ROLE_EMPLOYEE")
                        .build();
            }
            Customer customer = customerRepo.findByUsername(username);
            if (customer != null) {
                // trả về đối tượng User cho Customer
                return org.springframework.security.core.userdetails.User
                        .withUsername(customer.getUsername())
                        .password(customer.getPassword())
                        .authorities("ROLE_CUSTOMER")
                        .build();
            }
            throw new UsernameNotFoundException("User not found");
        };
    }
    @Configuration
    @Order(2)
    public static class CustomerSecurity {
        private final CustomAuthenticationSuccessHandlerForCustomer customAuthenticationSuccessHandlerForCustomer;

        // Constructor injection cho handler
        public CustomerSecurity(CustomAuthenticationSuccessHandlerForCustomer customAuthenticationSuccessHandlerForCustomer) {
            this.customAuthenticationSuccessHandlerForCustomer = customAuthenticationSuccessHandlerForCustomer;
        }

        @Bean
        public SecurityFilterChain customerSecurityFilterChain(HttpSecurity http) throws Exception {
            http
                    .securityMatcher("/customer/**","/home/**","/**") // Áp dụng cho các URL bắt đầu với /customer/
                    .authorizeHttpRequests(authorize -> authorize
                            .requestMatchers("/customer/login","/customer/register","/home/**","/**","/customer/index"
                            ,"/customer/forgot-password","/customer/verify-code","/customer/reset-password").permitAll() // Trang login của customer không yêu cầu xác thực
                            .anyRequest().hasAuthority("ROLE_CUSTOMER") // Các trang khác yêu cầu xác thực
                    )
                    .formLogin(form -> form
                            .loginPage("/customer/login")
                            .successHandler(customAuthenticationSuccessHandlerForCustomer)// Xử lý khi đăng nhập thành công
                            .failureUrl("/customer/login?error=true") // URL chuyển hướng khi đăng nhập thất bại
                            .permitAll()

                    )
                    .logout(logout -> logout
                            .logoutUrl("/customer/logout")
                            .logoutSuccessUrl("/?logout=true")
                            .invalidateHttpSession(true)
                            .deleteCookies("JSESSIONID")
                            .permitAll()
                    );
            return http.build();
        }
    }

    // Cấu hình AdminSecurity cho /admin/** và /employee/**
    @Configuration
        @Order(1)
        public static class AdminSecurity {
            @Bean
            public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
                http
                        .securityMatcher("/admin/**", "/employee/**") // Áp dụng cho các URL bắt đầu với /admin/ và /employee/
                        .authorizeHttpRequests(authorize -> authorize
                                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers("/employee/**").hasAuthority("ROLE_EMPLOYEE")
                                .anyRequest().permitAll()
                        )
                        .formLogin(form -> form
                                .loginPage("/admin/login")
                                .successHandler(new CustomAuthenticationSuccessHandler()) // Xử lý khi đăng nhập thành công
                                .failureUrl("/admin/login?error=true")
                                .permitAll()
                        )
                        .logout(logout -> logout
                                .logoutUrl("/admin/logout")
                                .logoutSuccessUrl("/admin/login?logout=true")
                                .permitAll()
                        );
                return http.build();
            }

        // AuthenticationManager để quản lý việc xác thực

    }
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);
        auth.userDetailsService(userDetailsService()).passwordEncoder(encoder());
        return auth.build();
    }
}
