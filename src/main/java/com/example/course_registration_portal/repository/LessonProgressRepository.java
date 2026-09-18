package com.example.course_registration_portal.repository;

import com.example.course_registration_portal.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    List<LessonProgress> findByUserIdAndCourseId(Long userId, Long courseId);
    Optional<LessonProgress> findByUserIdAndTopicId(Long userId, Long topicId);
    long countByUserIdAndCourseIdAndCompletedTrue(Long userId, Long courseId);
    boolean existsByUserIdAndTopicIdAndCompletedTrue(Long userId, Long topicId);
}
