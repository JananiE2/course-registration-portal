package com.example.course_registration_portal.service;

import com.example.course_registration_portal.entity.*;
import com.example.course_registration_portal.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LearningService {

    private final RegistrationRepository registrationRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final OptionalCourseContentRepository contentRepository;
    private final LessonProgressRepository progressRepository;

    public LearningService(RegistrationRepository registrationRepository,
                           CourseRepository courseRepository,
                           UserRepository userRepository,
                           OptionalCourseContentRepository contentRepository,
                           LessonProgressRepository progressRepository) {
        this.registrationRepository = registrationRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.contentRepository = contentRepository;
        this.progressRepository = progressRepository;
    }

    public boolean hasConfirmedRegistration(Long userId, Long courseId) {
        Optional<Registration> reg = registrationRepository.findByUserIdAndCourseId(userId, courseId);
        return reg.isPresent() && "CONFIRMED".equalsIgnoreCase(reg.get().getStatus());
    }

    public int getCourseProgressPercentage(Long userId, Long courseId) {
        List<OptionalCourseContent> topics = contentRepository.findByCourseIdOrderBySequenceNoAsc(courseId);
        if (topics.isEmpty()) {
            return 0;
        }

        long completed = progressRepository.countByUserIdAndCourseIdAndCompletedTrue(userId, courseId);
        return (int) Math.round(((double) completed / topics.size()) * 100.0);
    }

    public Set<Long> getCompletedTopicIds(Long userId, Long courseId) {
        List<LessonProgress> list = progressRepository.findByUserIdAndCourseId(userId, courseId);
        return list.stream()
                .filter(LessonProgress::isCompleted)
                .map(lp -> lp.getTopic().getId())
                .collect(Collectors.toSet());
    }

    public long getCompletedTopicsCount(Long userId, Long courseId) {
        return progressRepository.countByUserIdAndCourseIdAndCompletedTrue(userId, courseId);
    }

    @Transactional
    public boolean toggleTopicCompletion(Long userId, Long courseId, Long topicId) {
        if (!hasConfirmedRegistration(userId, courseId)) {
            throw new IllegalStateException("Classroom access requires a CONFIRMED course registration.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        OptionalCourseContent topic = contentRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));

        Optional<LessonProgress> existing = progressRepository.findByUserIdAndTopicId(userId, topicId);
        if (existing.isPresent()) {
            LessonProgress lp = existing.get();
            boolean newState = !lp.isCompleted();
            lp.setCompleted(newState);
            progressRepository.save(lp);
            return newState;
        } else {
            LessonProgress lp = new LessonProgress(user, course, topic, true);
            progressRepository.save(lp);
            return true;
        }
    }

    public boolean isCourseFullyCompleted(Long userId, Long courseId) {
        List<OptionalCourseContent> topics = contentRepository.findByCourseIdOrderBySequenceNoAsc(courseId);
        if (topics.isEmpty()) {
            return false;
        }
        long completed = progressRepository.countByUserIdAndCourseIdAndCompletedTrue(userId, courseId);
        return completed >= topics.size();
    }
}
