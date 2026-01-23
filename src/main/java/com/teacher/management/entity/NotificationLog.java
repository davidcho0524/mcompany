package com.teacher.management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_notification_log")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    @Column(nullable = false)
    private String notificationType; // 1DAY, 1HOUR

    @Column(nullable = false)
    private String status; // SUCCESS, FAIL

    private String messageId; // Solapi message ID

    private String failReason;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime sentAt;
}
