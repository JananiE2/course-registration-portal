package com.example.course_registration_portal.controller;

import com.example.course_registration_portal.dto.LoginDto;
import com.example.course_registration_portal.dto.RegisterDto;
import com.example.course_registration_portal.entity.User;
import com.example.course_registration_portal.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "redirect", required = false) String redirect,
                            HttpSession session,
                            Model model) {
        // If already logged in, redirect to role-specific dashboard
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser != null) {
            return "ADMIN".equalsIgnoreCase(loggedInUser.getRole())
                    ? "redirect:/admin/dashboard"
                    : "redirect:/student/dashboard";
        }

        model.addAttribute("loginDto", new LoginDto());
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }
        model.addAttribute("redirect", redirect);
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(@Valid @ModelAttribute("loginDto") LoginDto loginDto,
                              BindingResult bindingResult,
                              @RequestParam(value = "redirect", required = false) String redirect,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "login";
        }

        Optional<User> userOpt = userService.authenticate(loginDto.getEmail(), loginDto.getPassword());
        if (userOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Invalid email or password.");
            return "login";
        }

        User user = userOpt.get();
        session.setAttribute("loggedInUser", user);
        session.setAttribute("userRole", user.getRole());
        session.setAttribute("userName", user.getName());
        session.setAttribute("userId", user.getId());

        redirectAttributes.addFlashAttribute("successMessage", "Welcome back, " + user.getName() + "!");

        if (redirect != null && !redirect.trim().isEmpty() && redirect.startsWith("/")) {
            return "redirect:" + redirect;
        }

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return "redirect:/admin/dashboard";
        } else {
            return "redirect:/student/dashboard";
        }
    }

    @GetMapping("/register")
    public String registerPage(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser != null) {
            return "redirect:/student/dashboard";
        }
        model.addAttribute("registerDto", new RegisterDto());
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(@Valid @ModelAttribute("registerDto") RegisterDto registerDto,
                                 BindingResult bindingResult,
                                 HttpSession session,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            User student = userService.registerStudent(registerDto);
            // Automatically log in newly registered student
            session.setAttribute("loggedInUser", student);
            session.setAttribute("userRole", student.getRole());
            session.setAttribute("userName", student.getName());
            session.setAttribute("userId", student.getId());

            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Welcome to Gateway Software Solutions, " + student.getName() + ".");
            return "redirect:/courses";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login?logout=true";
    }
}
