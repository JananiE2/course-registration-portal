package com.example.course_registration_portal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Course name is required")
    @Column(name = "course_name", nullable = false)
    private String courseName;

    @Column(name = "category")
    private String category;

    @Column(name = "description", length = 3000)
    private String description;

    @Column(name = "duration")
    private String duration; // e.g. "8 Weeks", "12 Weeks"

    @Column(name = "fee")
    private BigDecimal fee;

    @Column(name = "status", nullable = false)
    private String status; // "ACTIVE" or "INACTIVE"

    @Column(name = "trainer_name")
    private String trainerName;

    @Column(name = "trainer_email")
    private String trainerEmail;

    @Column(name = "trainer_bio", length = 1000)
    private String trainerBio;

    @Column(name = "class_schedule")
    private String classSchedule; // e.g. "Mon - Fri, 10:00 AM - 12:00 PM IST"

    @Column(name = "meeting_link", length = 500)
    private String meetingLink;

    @Column(name = "meeting_platform")
    private String meetingPlatform; // e.g. "Google Meet", "Zoom"

    @Column(name = "classroom_notice", length = 1000)
    private String classroomNotice;

    @Column(name = "materials_url", length = 500)
    private String materialsUrl;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceNo ASC")
    private List<OptionalCourseContent> topics = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "ACTIVE";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Course() {}

    public Course(String courseName, String category, String description, String duration, BigDecimal fee, String status) {
        this.courseName = courseName;
        this.category = category;
        this.description = description;
        this.duration = duration;
        this.fee = fee;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OptionalCourseContent> getTopics() {
        return topics;
    }

    public void setTopics(List<OptionalCourseContent> topics) {
        this.topics = topics;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getTrainerName() {
        return trainerName;
    }

    public void setTrainerName(String trainerName) {
        this.trainerName = trainerName;
    }

    public String getTrainerEmail() {
        return trainerEmail;
    }

    public void setTrainerEmail(String trainerEmail) {
        this.trainerEmail = trainerEmail;
    }

    public String getTrainerBio() {
        return trainerBio;
    }

    public void setTrainerBio(String trainerBio) {
        this.trainerBio = trainerBio;
    }

    public String getClassSchedule() {
        return classSchedule;
    }

    public void setClassSchedule(String classSchedule) {
        this.classSchedule = classSchedule;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
    }

    public String getMeetingPlatform() {
        return meetingPlatform;
    }

    public void setMeetingPlatform(String meetingPlatform) {
        this.meetingPlatform = meetingPlatform;
    }

    public String getClassroomNotice() {
        return classroomNotice;
    }

    public void setClassroomNotice(String classroomNotice) {
        this.classroomNotice = classroomNotice;
    }

    public String getMaterialsUrl() {
        return materialsUrl;
    }

    public void setMaterialsUrl(String materialsUrl) {
        this.materialsUrl = materialsUrl;
    }
}
