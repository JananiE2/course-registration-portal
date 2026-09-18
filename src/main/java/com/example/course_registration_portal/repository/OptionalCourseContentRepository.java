package com.example.course_registration_portal.repository;

import com.example.course_registration_portal.entity.OptionalCourseContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptionalCourseContentRepository extends JpaRepository<OptionalCourseContent, Long> {
    List<OptionalCourseContent> findByCourseIdOrderBySequenceNoAsc(Long courseId);
}
