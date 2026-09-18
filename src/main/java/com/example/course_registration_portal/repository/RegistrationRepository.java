package com.example.course_registration_portal.repository;

import com.example.course_registration_portal.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByUserIdOrderByRegistrationDateDesc(Long userId);
    Optional<Registration> findByUserIdAndCourseId(Long userId, Long courseId);
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    List<Registration> findAllByOrderByRegistrationDateDesc();

    long countByStatus(String status);

    @Query("SELECT r FROM Registration r WHERE " +
           "(:status IS NULL OR :status = '' OR r.status = :status) AND " +
           "(:courseId IS NULL OR r.course.id = :courseId) " +
           "ORDER BY r.registrationDate DESC")
    List<Registration> filterRegistrations(@Param("status") String status, @Param("courseId") Long courseId);
}
