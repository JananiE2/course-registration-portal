package com.example.course_registration_portal.repository;

import com.example.course_registration_portal.entity.AssessmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentSubmissionRepository extends JpaRepository<AssessmentSubmission, Long> {
    boolean existsByUserIdAndCourseIdAndPassedTrue(Long userId, Long courseId);
    Optional<AssessmentSubmission> findTopByUserIdAndCourseIdOrderBySubmittedAtDesc(Long userId, Long courseId);
    List<AssessmentSubmission> findByUserIdAndCourseIdOrderBySubmittedAtDesc(Long userId, Long courseId);
    long countByUserIdAndCourseId(Long userId, Long courseId);
}
