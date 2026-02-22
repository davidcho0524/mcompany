package com.teacher.management.repository;

import com.teacher.management.entity.Lecture;
import com.teacher.management.entity.LectureNotificationConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LectureNotificationConfigRepository extends JpaRepository<LectureNotificationConfig, Long> {
    List<LectureNotificationConfig> findByLecture(Lecture lecture);

    Optional<LectureNotificationConfig> findByLectureAndTimingType(Lecture lecture, String timingType);

    void deleteByLectureId(Long lectureId);
}
