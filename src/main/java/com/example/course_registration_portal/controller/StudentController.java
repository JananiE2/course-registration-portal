package com.example.course_registration_portal.controller;

import com.example.course_registration_portal.dto.ProfileDto;
import com.example.course_registration_portal.entity.Course;
import com.example.course_registration_portal.entity.Registration;
import com.example.course_registration_portal.entity.User;
import com.example.course_registration_portal.service.CourseService;
import com.example.course_registration_portal.service.RegistrationService;
import com.example.course_registration_portal.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class StudentController {

    private final UserService userService;
    private final CourseService courseService;
    private final RegistrationService registrationService;
    private final com.example.course_registration_portal.service.LearningService learningService;
    private final com.example.course_registration_portal.service.AssessmentService assessmentService;

    public StudentController(UserService userService,
                             CourseService courseService,
                             RegistrationService registrationService,
                             com.example.course_registration_portal.service.LearningService learningService,
                             com.example.course_registration_portal.service.AssessmentService assessmentService) {
        this.userService = userService;
        this.courseService = courseService;
        this.registrationService = registrationService;
        this.learningService = learningService;
        this.assessmentService = assessmentService;
    }

    private User getLoggedInUser(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }

    @GetMapping("/student/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = getLoggedInUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        // Refresh user details from DB
        user = userService.findById(user.getId()).orElse(user);
        session.setAttribute("loggedInUser", user);

        List<Registration> registrations = registrationService.getStudentRegistrations(user.getId());
        long pendingCount = registrations.stream().filter(r -> "PENDING".equalsIgnoreCase(r.getStatus())).count();
        long confirmedCount = registrations.stream().filter(r -> "CONFIRMED".equalsIgnoreCase(r.getStatus())).count();

        // Calculate progress percentage for confirmed courses
        java.util.Map<Long, Integer> courseProgressMap = new java.util.HashMap<>();
        for (Registration reg : registrations) {
            if ("CONFIRMED".equalsIgnoreCase(reg.getStatus()) && reg.getCourse() != null) {
                courseProgressMap.put(reg.getCourse().getId(), learningService.getCourseProgressPercentage(user.getId(), reg.getCourse().getId()));
            }
        }

        model.addAttribute("user", user);
        model.addAttribute("registrations", registrations);
        model.addAttribute("totalRegistrations", registrations.size());
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("confirmedCount", confirmedCount);
        model.addAttribute("courseProgressMap", courseProgressMap);

        return "student/dashboard";
    }

    @GetMapping("/student/learn/{courseId}")
    public String viewClassroom(@PathVariable("courseId") Long courseId,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            return "redirect:/login?redirect=/student/learn/" + courseId;
        }

        if (!learningService.hasConfirmedRegistration(user.getId(), courseId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Classroom access is available only after Gateway administration CONFIRMS your registration.");
            return "redirect:/student/dashboard";
        }

        Optional<Course> courseOpt = courseService.getCourseById(courseId);
        if (courseOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Course not found.");
            return "redirect:/student/dashboard";
        }

        Course course = courseOpt.get();
        int progress = learningService.getCourseProgressPercentage(user.getId(), courseId);
        java.util.Set<Long> completedIds = learningService.getCompletedTopicIds(user.getId(), courseId);
        long completedCount = learningService.getCompletedTopicsCount(user.getId(), courseId);
        boolean isComplete = learningService.isCourseFullyCompleted(user.getId(), courseId);
        boolean hasPassedAssessment = assessmentService.hasPassedAssessment(user.getId(), courseId);
        Optional<com.example.course_registration_portal.entity.AssessmentSubmission> latestAssessment = assessmentService.getLatestSubmission(user.getId(), courseId);

        model.addAttribute("course", course);
        model.addAttribute("topics", course.getTopics());
        model.addAttribute("progress", progress);
        model.addAttribute("completedIds", completedIds);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("isComplete", isComplete);
        model.addAttribute("hasPassedAssessment", hasPassedAssessment);
        model.addAttribute("latestAssessment", latestAssessment.orElse(null));
        model.addAttribute("user", user);

        return "student/classroom";
    }

    @PostMapping("/student/learn/{courseId}/toggle/{topicId}")
    public String toggleTopic(@PathVariable("courseId") Long courseId,
                              @PathVariable("topicId") Long topicId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            boolean completed = learningService.toggleTopicCompletion(user.getId(), courseId, topicId);
            redirectAttributes.addFlashAttribute("successMessage", completed ? "Module marked as completed! Keep up the great work!" : "Module marked as incomplete.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/student/learn/" + courseId;
    }

    @GetMapping("/student/learn/{courseId}/assessment")
    public String takeAssessment(@PathVariable("courseId") Long courseId,
                                 HttpSession session,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        if (!learningService.hasConfirmedRegistration(user.getId(), courseId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You must have a confirmed registration to take the final course assessment.");
            return "redirect:/student/dashboard";
        }

        if (!learningService.isCourseFullyCompleted(user.getId(), courseId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Assessment Locked: You must complete all syllabus curriculum modules (100%) before taking the final assessment.");
            return "redirect:/student/learn/" + courseId;
        }

        Course course = courseService.getCourseById(courseId).orElse(null);
        if (course == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Course not found.");
            return "redirect:/student/dashboard";
        }

        List<com.example.course_registration_portal.dto.AssessmentQuestion> questions = assessmentService.getQuestionsForCourse(course);
        boolean hasPassed = assessmentService.hasPassedAssessment(user.getId(), courseId);
        Optional<com.example.course_registration_portal.entity.AssessmentSubmission> latest = assessmentService.getLatestSubmission(user.getId(), courseId);

        model.addAttribute("course", course);
        model.addAttribute("questions", questions);
        model.addAttribute("hasPassed", hasPassed);
        model.addAttribute("latestSubmission", latest.orElse(null));
        model.addAttribute("passingScore", com.example.course_registration_portal.service.AssessmentService.PASSING_SCORE_PERCENTAGE);
        model.addAttribute("user", user);

        return "student/assessment";
    }

    @PostMapping("/student/learn/{courseId}/assessment")
    public String submitAssessment(@PathVariable("courseId") Long courseId,
                                   @RequestParam Map<String, String> answers,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            com.example.course_registration_portal.entity.AssessmentSubmission submission = assessmentService.evaluateAndSave(user.getId(), courseId, answers);
            return "redirect:/student/learn/" + courseId + "/assessment/result";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/student/learn/" + courseId;
        }
    }

    @GetMapping("/student/learn/{courseId}/assessment/result")
    public String viewAssessmentResult(@PathVariable("courseId") Long courseId,
                                       HttpSession session,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        Optional<com.example.course_registration_portal.entity.AssessmentSubmission> latestOpt = assessmentService.getLatestSubmission(user.getId(), courseId);
        if (latestOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "No assessment submission found. Please take the assessment first.");
            return "redirect:/student/learn/" + courseId;
        }

        Course course = courseService.getCourseById(courseId).orElse(null);
        if (course == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Course not found.");
            return "redirect:/student/dashboard";
        }

        com.example.course_registration_portal.entity.AssessmentSubmission submission = latestOpt.get();
        model.addAttribute("course", course);
        model.addAttribute("submission", submission);
        model.addAttribute("passingScore", com.example.course_registration_portal.service.AssessmentService.PASSING_SCORE_PERCENTAGE);
        model.addAttribute("user", user);

        return "student/assessment-result";
    }

    @GetMapping("/student/learn/{courseId}/certificate")
    public String viewCertificate(@PathVariable("courseId") Long courseId,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        if (!learningService.hasConfirmedRegistration(user.getId(), courseId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You must have a confirmed registration to view course completion credentials.");
            return "redirect:/student/dashboard";
        }

        if (!learningService.isCourseFullyCompleted(user.getId(), courseId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please complete all syllabus curriculum modules to unlock your Course Completion Certificate.");
            return "redirect:/student/learn/" + courseId;
        }

        // Enforce Final Assessment requirement
        if (!assessmentService.hasPassedAssessment(user.getId(), courseId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Certificate Locked: You must take and pass the final course assessment (minimum 60% required) before you can generate or view your official completion certificate.");
            return "redirect:/student/learn/" + courseId;
        }

        Course course = courseService.getCourseById(courseId).orElseThrow();
        Optional<Registration> regOpt = registrationService.getStudentRegistrations(user.getId()).stream()
                .filter(r -> r.getCourse().getId().equals(courseId))
                .findFirst();

        java.time.LocalDate startDate = regOpt.map(r -> r.getRegistrationDate().toLocalDate()).orElse(java.time.LocalDate.now().minusDays(30));
        java.time.LocalDate completionDate = java.time.LocalDate.now();

        model.addAttribute("user", user);
        model.addAttribute("course", course);
        model.addAttribute("startDate", startDate);
        model.addAttribute("completionDate", completionDate);
        model.addAttribute("registration", regOpt.orElse(null));
        model.addAttribute("certificateId", "GSS-CERT-" + courseId + "-" + user.getId() + "-" + completionDate.getYear());

        return "student/certificate";
    }

    @GetMapping("/student/profile")
    public String profile(HttpSession session, Model model) {
        User user = getLoggedInUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        user = userService.findById(user.getId()).orElse(user);
        ProfileDto dto = new ProfileDto();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setDepartment(user.getDepartment());

        model.addAttribute("profileDto", dto);
        model.addAttribute("user", user);
        return "student/profile";
    }

    @PostMapping("/student/profile")
    public String updateProfile(@Valid @ModelAttribute("profileDto") ProfileDto profileDto,
                                BindingResult bindingResult,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            return "student/profile";
        }

        try {
            User updated = userService.updateProfile(user.getId(), profileDto);
            session.setAttribute("loggedInUser", updated);
            session.setAttribute("userName", updated.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
            return "redirect:/student/profile";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("user", user);
            return "student/profile";
        }
    }

    @GetMapping("/register-course/{id}")
    public String showCourseRegistrationForm(@PathVariable("id") Long id,
                                             HttpSession session,
                                             Model model,
                                             RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            return "redirect:/login?redirect=/register-course/" + id;
        }

        Optional<Course> courseOpt = courseService.getCourseById(id);
        if (courseOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Course not found.");
            return "redirect:/courses";
        }

        Course course = courseOpt.get();

        // Duplicate check
        if (registrationService.isAlreadyRegistered(user.getId(), course.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You are already registered for '" + course.getCourseName() + "'. Please check your dashboard.");
            return "redirect:/student/dashboard";
        }

        model.addAttribute("course", course);
        model.addAttribute("user", user);
        return "student/register-course";
    }

    @PostMapping("/register-course/{id}")
    public String processCourseRegistration(@PathVariable("id") Long id,
                                            @RequestParam(value = "remarks", required = false) String remarks,
                                            HttpSession session,
                                            RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            return "redirect:/login?redirect=/register-course/" + id;
        }

        try {
            Registration registration = registrationService.registerStudentForCourse(user.getId(), id, remarks);
            redirectAttributes.addFlashAttribute("successMessage", "Registration submitted successfully!");
            return "redirect:/registration/" + registration.getId();
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/courses/" + id;
        }
    }

    @GetMapping("/registration/{id}")
    public String showConfirmation(@PathVariable("id") Long id,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Registration> regOpt = registrationService.getRegistrationById(id);
        if (regOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Registration not found.");
            return "redirect:/student/dashboard";
        }

        Registration registration = regOpt.get();

        // Check if student owns this registration or is admin
        if (!"ADMIN".equalsIgnoreCase(user.getRole()) && !registration.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied.");
            return "redirect:/student/dashboard";
        }

        model.addAttribute("registration", registration);
        return "student/confirmation";
    }
}
