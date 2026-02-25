package com.teacher.management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "tb_lecture")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    private LectureCategory category; // Lecture Category

    private BigDecimal price;

    @Column(name = "total_hours")
    private Double totalHours;

    @Column(name = "lecture_type")
    private String lectureType; // 비대면, 대면, 하이브리드

    private String location; // 강의장소

    @Column(name = "attendee_count")
    private Integer attendeeCount; // 수강인원

    @Column(name = "is_paid")
    private Boolean isPaid = false;

    @Column(name = "lecture_at")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime lectureAt; // Date and Time of Lecture

    private String status; // SCHEDULED, COMPLETED, CANCELLED

    @Column(name = "notification_yn", length = 1)
    private String notificationYn = "N";

    // Feedback Fields
    @Column(columnDefinition = "TEXT")
    private String lectureMaterials; // 강의자료

    @Column(columnDefinition = "TEXT")
    private String downloadLink; // 다운로드링크

    @Column(columnDefinition = "TEXT")
    private String customerEvaluation; // 고객 대상의 강의평가

    @Column(columnDefinition = "TEXT")
    private String selfReviewContent; // 자가리뷰 (콘텐츠)

    @Column(columnDefinition = "TEXT")
    private String selfReviewSpeed; // 자가리뷰 (속도)

    @Column(columnDefinition = "TEXT")
    private String selfReviewDelivery; // 자가리뷰 (음,어 등의 부사사용)

    @Column(columnDefinition = "TEXT")
    private String mentorFeedback; // 멘토피드백

    @Column(columnDefinition = "TEXT")
    private String improvements; // 개선사항

    @Column(columnDefinition = "TEXT")
    private String linkedinPost; // 링크드인 작성

    @Column(columnDefinition = "TEXT")
    private String naverBlogPost; // 네이버 작성

    @Column(columnDefinition = "TEXT")
    private String wordpressPost; // 워드프레스 작성

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
