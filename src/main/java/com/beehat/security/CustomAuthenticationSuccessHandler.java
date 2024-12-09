package com.beehat.security;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        org.springframework.security.core.Authentication authentication) throws IOException, ServletException {
        String role = authentication.getAuthorities().stream()
                .findFirst().get().getAuthority();

        if (role.equals("ROLE_ADMIN")) {
            response.sendRedirect("/admin/index");
        } else if(role.equals("ROLE_EMPLOYEE")){
            response.sendRedirect("/employee/dashboard");
        } else {
            response.sendRedirect("/admin/login?error=true");
        }
    }


}
