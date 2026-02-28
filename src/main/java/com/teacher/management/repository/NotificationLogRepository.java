package com.teacher.management.repository;

import com.teacher.management.entity.Lecture;
import com.teacher.management.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    boolean existsByLectureAndNotificationTypeAndStatus(Lecture lecture, String notificationType, String status);

    boolean existsByLectureAndNotificationTypeAndStatusAndMemberId(Lecture lecture, String notificationType,
            String status, Long memberId);

    List<NotificationLog> findByLectureId(Long lectureId);

    void deleteByLectureId(Long lectureId);
}
