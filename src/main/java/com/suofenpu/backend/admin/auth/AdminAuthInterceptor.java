package com.suofenpu.backend.admin.auth;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {
    private final AdminAuthService authService;

    public AdminAuthInterceptor(AdminAuthService authService) { this.authService = authService; }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        authService.requireAdmin(request.getHeader("Authorization"));
        return true;
    }
}
