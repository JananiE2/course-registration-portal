package com.example.course_registration_portal.service;

import com.example.course_registration_portal.entity.Course;
import com.example.course_registration_portal.entity.Registration;
import com.example.course_registration_portal.entity.User;
import com.example.course_registration_portal.repository.CourseRepository;
import com.example.course_registration_portal.repository.RegistrationRepository;
import com.example.course_registration_portal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public RegistrationService(RegistrationRepository registrationRepository,
                               UserRepository userRepository,
                               CourseRepository courseRepository) {
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public Registration registerStudentForCourse(Long userId, Long courseId, String remarks) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (!"ACTIVE".equalsIgnoreCase(course.getStatus())) {
            throw new IllegalStateException("Cannot register for an inactive course.");
        }

        // Duplicate registration check
        Optional<Registration> existing = registrationRepository.findByUserIdAndCourseId(userId, courseId);
        if (existing.isPresent()) {
            Registration reg = existing.get();
            if (!"CANCELLED".equalsIgnoreCase(reg.getStatus())) {
                throw new IllegalStateException("You are already registered for this course (" + course.getCourseName() + "). Current status: " + reg.getStatus());
            } else {
                // Reactivate cancelled registration
                reg.setStatus("PENDING");
                reg.setRegistrationDate(LocalDateTime.now());
                reg.setRemarks(remarks);
                return registrationRepository.save(reg);
            }
        }

        Registration registration = new Registration();
        registration.setUser(user);
        registration.setCourse(course);
        registration.setStatus("PENDING");
        registration.setRegistrationDate(LocalDateTime.now());
        registration.setRemarks(remarks);

        return registrationRepository.save(registration);
    }

    public boolean isAlreadyRegistered(Long userId, Long courseId) {
        Optional<Registration> existing = registrationRepository.findByUserIdAndCourseId(userId, courseId);
        return existing.isPresent() && !"CANCELLED".equalsIgnoreCase(existing.get().getStatus());
    }

    public List<Registration> getStudentRegistrations(Long userId) {
        return registrationRepository.findByUserIdOrderByRegistrationDateDesc(userId);
    }

    public Optional<Registration> getRegistrationById(Long id) {
        return registrationRepository.findById(id);
    }

    public List<Registration> getAllRegistrations() {
        return registrationRepository.findAllByOrderByRegistrationDateDesc();
    }

    public List<Registration> filterRegistrations(String status, Long courseId) {
        return registrationRepository.filterRegistrations(status, courseId);
    }

    @Transactional
    public Registration updateStatus(Long registrationId, String newStatus, String remarks) {
        Registration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found with id: " + registrationId));

        reg.setStatus(newStatus.toUpperCase());
        if (remarks != null && !remarks.trim().isEmpty()) {
            reg.setRemarks(remarks.trim());
        }
        return registrationRepository.save(reg);
    }

    public long countTotalRegistrations() {
        return registrationRepository.count();
    }

    public long countByStatus(String status) {
        return registrationRepository.countByStatus(status);
    }
}
