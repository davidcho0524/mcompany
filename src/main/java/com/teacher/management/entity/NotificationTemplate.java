package com.teacher.management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_notification_template")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Template Name (e.g., "1 Day Before - Standard")

    @Column(nullable = false)
    private String messageType; // SMS, KAKAO

    @Lob
    @Column(nullable = false)
    private String content;

    // Kakao Specific
    private String kakaoPfId; // Plus Friend ID (Channel ID)
    private String kakaoTemplateId; // Kakao Template ID

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
