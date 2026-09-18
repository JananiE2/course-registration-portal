package com.example.course_registration_portal.controller;

import com.example.course_registration_portal.entity.Course;
import com.example.course_registration_portal.entity.User;
import com.example.course_registration_portal.service.CourseService;
import com.example.course_registration_portal.service.RegistrationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class CourseController {

    private final CourseService courseService;
    private final RegistrationService registrationService;

    public CourseController(CourseService courseService, RegistrationService registrationService) {
        this.courseService = courseService;
        this.registrationService = registrationService;
    }

    @GetMapping("/courses")
    public String listCourses(@RequestParam(value = "category", required = false) String category,
                              @RequestParam(value = "keyword", required = false) String keyword,
                              Model model) {
        List<Course> courses = courseService.searchCourses(category, keyword);
        List<String> categories = courseService.getCategories();

        model.addAttribute("courses", courses);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("keyword", keyword);
        model.addAttribute("totalResults", courses.size());

        return "courses/list";
    }

    @GetMapping("/courses/{id}")
    public String courseDetails(@PathVariable("id") Long id,
                                HttpSession session,
                                Model model) {
        Optional<Course> courseOpt = courseService.getCourseById(id);
        if (courseOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Course not found.");
            return "error/404";
        }

        Course course = courseOpt.get();
        model.addAttribute("course", course);

        // Check if current user is logged in and already registered
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        boolean isRegistered = false;
        if (loggedInUser != null) {
            isRegistered = registrationService.isAlreadyRegistered(loggedInUser.getId(), course.getId());
        }
        model.addAttribute("isRegistered", isRegistered);

        return "courses/details";
    }
}
