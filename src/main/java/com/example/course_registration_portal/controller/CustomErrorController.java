package com.example.course_registration_portal.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        int statusCode = 500;
        if (status != null) {
            statusCode = Integer.parseInt(status.toString());
        }

        model.addAttribute("statusCode", statusCode);
        model.addAttribute("statusMessage", HttpStatus.resolve(statusCode) != null ? HttpStatus.resolve(statusCode).getReasonPhrase() : "Error");
        model.addAttribute("errorMessage", message != null && !message.toString().isEmpty() ? message.toString() : "An unexpected situation occurred.");

        if (statusCode == 404) {
            return "error/404";
        }
        return "error/500";
    }
}
