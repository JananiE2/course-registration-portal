package com.example.course_registration_portal.controller;

import com.example.course_registration_portal.dto.CourseDto;
import com.example.course_registration_portal.entity.Course;
import com.example.course_registration_portal.entity.Registration;
import com.example.course_registration_portal.service.CourseService;
import com.example.course_registration_portal.service.RegistrationService;
import com.example.course_registration_portal.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CourseService courseService;
    private final UserService userService;
    private final RegistrationService registrationService;

    public AdminController(CourseService courseService, UserService userService, RegistrationService registrationService) {
        this.courseService = courseService;
        this.userService = userService;
        this.registrationService = registrationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalCourses", courseService.countTotalCourses());
        model.addAttribute("activeCourses", courseService.countActiveCourses());
        model.addAttribute("totalStudents", userService.countStudents());
        model.addAttribute("totalRegistrations", registrationService.countTotalRegistrations());
        model.addAttribute("pendingRegistrations", registrationService.countByStatus("PENDING"));
        model.addAttribute("confirmedRegistrations", registrationService.countByStatus("CONFIRMED"));

        List<Registration> recentRegistrations = registrationService.getAllRegistrations().stream().limit(5).toList();
        model.addAttribute("recentRegistrations", recentRegistrations);

        return "admin/dashboard";
    }

    @GetMapping("/courses")
    public String listCourses(@RequestParam(value = "category", required = false) String category,
                              @RequestParam(value = "keyword", required = false) String keyword,
                              Model model) {
        List<Course> courses = courseService.searchAllCoursesAdmin(category, keyword);
        model.addAttribute("courses", courses);
        model.addAttribute("categories", courseService.getCategories());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("keyword", keyword);
        return "admin/courses";
    }

    @GetMapping("/courses/new")
    public String newCourseForm(Model model) {
        model.addAttribute("courseDto", new CourseDto());
        model.addAttribute("isEdit", false);
        return "admin/course-form";
    }

    @PostMapping("/courses/new")
    public String createCourse(@Valid @ModelAttribute("courseDto") CourseDto courseDto,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "admin/course-form";
        }

        courseService.createCourse(courseDto);
        redirectAttributes.addFlashAttribute("successMessage", "Course '" + courseDto.getCourseName() + "' created successfully!");
        return "redirect:/admin/courses";
    }

    @GetMapping("/courses/edit/{id}")
    public String editCourseForm(@PathVariable("id") Long id,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        Optional<Course> courseOpt = courseService.getCourseById(id);
        if (courseOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Course not found.");
            return "redirect:/admin/courses";
        }

        Course course = courseOpt.get();
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setCourseName(course.getCourseName());
        dto.setCategory(course.getCategory());
        dto.setDescription(course.getDescription());
        dto.setDuration(course.getDuration());
        dto.setFee(course.getFee());
        dto.setStatus(course.getStatus());
        dto.setTrainerName(course.getTrainerName());
        dto.setTrainerEmail(course.getTrainerEmail());
        dto.setTrainerBio(course.getTrainerBio());
        dto.setClassSchedule(course.getClassSchedule());
        dto.setMeetingLink(course.getMeetingLink());
        dto.setMeetingPlatform(course.getMeetingPlatform());
        dto.setClassroomNotice(course.getClassroomNotice());
        dto.setMaterialsUrl(course.getMaterialsUrl());

        model.addAttribute("courseDto", dto);
        model.addAttribute("isEdit", true);
        return "admin/course-form";
    }

    @PostMapping("/courses/edit/{id}")
    public String updateCourse(@PathVariable("id") Long id,
                               @Valid @ModelAttribute("courseDto") CourseDto courseDto,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            courseDto.setId(id);
            model.addAttribute("isEdit", true);
            return "admin/course-form";
        }

        try {
            courseService.updateCourse(id, courseDto);
            redirectAttributes.addFlashAttribute("successMessage", "Course updated successfully!");
            return "redirect:/admin/courses";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/courses";
        }
    }

    @PostMapping("/courses/{id}/toggle")
    public String toggleCourseStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            courseService.toggleStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Course status updated successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/courses";
    }

    @PostMapping("/courses/{id}/delete")
    public String deleteCourse(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            courseService.deleteCourse(id);
            redirectAttributes.addFlashAttribute("successMessage", "Course deactivated successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/courses";
    }

    @GetMapping("/registrations")
    public String listRegistrations(@RequestParam(value = "status", required = false) String status,
                                    @RequestParam(value = "courseId", required = false) Long courseId,
                                    Model model) {
        List<Registration> registrations = registrationService.filterRegistrations(status, courseId);
        model.addAttribute("registrations", registrations);
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCourseId", courseId);
        return "admin/registrations";
    }

    @PostMapping("/registrations/{id}/status")
    public String updateRegistrationStatus(@PathVariable("id") Long id,
                                           @RequestParam("status") String status,
                                           @RequestParam(value = "remarks", required = false) String remarks,
                                           RedirectAttributes redirectAttributes) {
        try {
            registrationService.updateStatus(id, status, remarks);
            redirectAttributes.addFlashAttribute("successMessage", "Registration status updated to " + status + "!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/registrations";
    }
}
