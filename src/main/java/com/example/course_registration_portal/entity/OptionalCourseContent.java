package com.example.course_registration_portal.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "optional_course_content")
public class OptionalCourseContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String topic;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "learning_objectives", columnDefinition = "TEXT")
    private String learningObjectives;

    @Column(name = "resource_link", length = 500)
    private String resourceLink;

    @Column(name = "duration_hours")
    private String durationHours; // e.g. "2 Hours"

    @Column(name = "sequence_no")
    private Integer sequenceNo;

    public OptionalCourseContent() {}

    public OptionalCourseContent(Course course, String topic, String description, Integer sequenceNo) {
        this.course = course;
        this.topic = topic;
        this.description = description;
        this.sequenceNo = sequenceNo;
    }

    public OptionalCourseContent(Course course, String topic, String description, Integer sequenceNo, String learningObjectives, String resourceLink, String durationHours) {
        this.course = course;
        this.topic = topic;
        this.description = description;
        this.sequenceNo = sequenceNo;
        this.learningObjectives = learningObjectives;
        this.resourceLink = resourceLink;
        this.durationHours = durationHours;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSequenceNo() {
        return sequenceNo;
    }

    public void setSequenceNo(Integer sequenceNo) {
        this.sequenceNo = sequenceNo;
    }

    public String getLearningObjectives() {
        return learningObjectives;
    }

    public void setLearningObjectives(String learningObjectives) {
        this.learningObjectives = learningObjectives;
    }

    public String getResourceLink() {
        return resourceLink;
    }

    public void setResourceLink(String resourceLink) {
        this.resourceLink = resourceLink;
    }

    public String getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(String durationHours) {
        this.durationHours = durationHours;
    }
}
