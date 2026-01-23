package com.teacher.management.repository;

import com.teacher.management.entity.Lecture;
import com.teacher.management.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    boolean existsByLectureAndNotificationType(Lecture lecture, String notificationType);
}
