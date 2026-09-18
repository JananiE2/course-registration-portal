package com.example.course_registration_portal.service;

import com.example.course_registration_portal.dto.CourseDto;
import com.example.course_registration_portal.entity.Course;
import com.example.course_registration_portal.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public List<Course> getActiveCourses() {
        return courseRepository.findByStatusOrderByCourseNameAsc("ACTIVE");
    }

    public List<Course> searchCourses(String category, String keyword) {
        return courseRepository.searchCourses("ACTIVE", category, keyword);
    }

    public List<Course> searchAllCoursesAdmin(String category, String keyword) {
        return courseRepository.searchCourses(null, category, keyword);
    }

    public Optional<Course> getCourseById(Long id) {
        return courseRepository.findById(id);
    }

    @Transactional
    public Course createCourse(CourseDto dto) {
        Course course = new Course();
        course.setCourseName(dto.getCourseName().trim());
        course.setCategory(dto.getCategory().trim());
        course.setDescription(dto.getDescription().trim());
        course.setDuration(dto.getDuration().trim());
        course.setFee(dto.getFee());
        course.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        course.setTrainerName(dto.getTrainerName());
        course.setTrainerEmail(dto.getTrainerEmail());
        course.setTrainerBio(dto.getTrainerBio());
        course.setClassSchedule(dto.getClassSchedule());
        course.setMeetingLink(dto.getMeetingLink());
        course.setMeetingPlatform(dto.getMeetingPlatform());
        course.setClassroomNotice(dto.getClassroomNotice());
        course.setMaterialsUrl(dto.getMaterialsUrl());
        return courseRepository.save(course);
    }

    @Transactional
    public Course updateCourse(Long id, CourseDto dto) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + id));

        course.setCourseName(dto.getCourseName().trim());
        course.setCategory(dto.getCategory().trim());
        course.setDescription(dto.getDescription().trim());
        course.setDuration(dto.getDuration().trim());
        course.setFee(dto.getFee());
        if (dto.getStatus() != null) {
            course.setStatus(dto.getStatus());
        }
        course.setTrainerName(dto.getTrainerName());
        course.setTrainerEmail(dto.getTrainerEmail());
        course.setTrainerBio(dto.getTrainerBio());
        course.setClassSchedule(dto.getClassSchedule());
        course.setMeetingLink(dto.getMeetingLink());
        course.setMeetingPlatform(dto.getMeetingPlatform());
        course.setClassroomNotice(dto.getClassroomNotice());
        course.setMaterialsUrl(dto.getMaterialsUrl());
        return courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long id) {
        // Soft delete / deactivate to protect foreign key constraints on historical registrations
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + id));
        course.setStatus("INACTIVE");
        courseRepository.save(course);
    }

    @Transactional
    public void toggleStatus(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + id));
        if ("ACTIVE".equalsIgnoreCase(course.getStatus())) {
            course.setStatus("INACTIVE");
        } else {
            course.setStatus("ACTIVE");
        }
        courseRepository.save(course);
    }

    public List<String> getCategories() {
        return courseRepository.findDistinctCategories();
    }

    public long countTotalCourses() {
        return courseRepository.count();
    }

    public long countActiveCourses() {
        return courseRepository.countByStatus("ACTIVE");
    }
}
