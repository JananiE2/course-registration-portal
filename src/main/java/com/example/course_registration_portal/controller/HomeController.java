package com.example.course_registration_portal.controller;

import com.example.course_registration_portal.entity.Course;
import com.example.course_registration_portal.service.CourseService;
import com.example.course_registration_portal.service.RegistrationService;
import com.example.course_registration_portal.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final CourseService courseService;
    private final UserService userService;
    private final RegistrationService registrationService;

    public HomeController(CourseService courseService, UserService userService, RegistrationService registrationService) {
        this.courseService = courseService;
        this.userService = userService;
        this.registrationService = registrationService;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Course> activeCourses = courseService.getActiveCourses();
        // Featured 6 courses for homepage
        List<Course> featuredCourses = activeCourses.stream().limit(6).toList();

        model.addAttribute("featuredCourses", featuredCourses);
        model.addAttribute("totalCourses", courseService.countTotalCourses());
        model.addAttribute("totalStudents", userService.countStudents());
        model.addAttribute("totalRegistrations", registrationService.countTotalRegistrations());

        return "index";
    }
}
