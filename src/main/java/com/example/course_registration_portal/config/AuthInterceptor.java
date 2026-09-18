package com.example.course_registration_portal.config;

import com.example.course_registration_portal.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        // 1. Admin endpoints require logged in ADMIN
        if (uri.startsWith("/admin")) {
            if (loggedInUser == null) {
                response.sendRedirect("/login?error=Please+login+to+access+the+admin+portal&redirect=" + uri);
                return false;
            }
            if (!"ADMIN".equalsIgnoreCase(loggedInUser.getRole())) {
                response.sendRedirect("/student/dashboard?error=Access+denied.+Administrator+privileges+required.");
                return false;
            }
            return true;
        }

        // 2. Student protected endpoints
        if (uri.startsWith("/student") || uri.startsWith("/register-course")) {
            if (loggedInUser == null) {
                response.sendRedirect("/login?error=Please+login+to+continue&redirect=" + uri);
                return false;
            }
            return true;
        }

        return true;
    }
}
