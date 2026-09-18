package com.example.course_registration_portal.repository;

import com.example.course_registration_portal.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByStatus(String status);
    List<Course> findByStatusOrderByCourseNameAsc(String status);
    List<Course> findByCategory(String category);
    long countByStatus(String status);

    @Query("SELECT DISTINCT c.category FROM Course c WHERE c.category IS NOT NULL ORDER BY c.category")
    List<String> findDistinctCategories();

    @Query("SELECT c FROM Course c WHERE (:status IS NULL OR c.status = :status) " +
           "AND (:category IS NULL OR :category = '' OR c.category = :category) " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(c.courseName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Course> searchCourses(@Param("status") String status,
                               @Param("category") String category,
                               @Param("keyword") String keyword);
}
