package com.teacher.management.service;

import com.teacher.management.entity.Lecture;
import com.teacher.management.repository.LectureNotificationConfigRepository;
import com.teacher.management.repository.LectureRepository;
import com.teacher.management.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LectureService {

    private final LectureRepository lectureRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final LectureNotificationConfigRepository lectureNotificationConfigRepository;

    public Page<Lecture> getAllLectures(Pageable pageable) {
        return lectureRepository.findAll(pageable);
    }

    public Lecture getLectureById(Long id) {
        return lectureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid lecture Id:" + id));
    }

    @Transactional
    public Lecture saveLecture(Lecture lecture) {
        return lectureRepository.save(lecture);
    }

    @Transactional
    public void deleteLecture(Long id) {
        notificationLogRepository.deleteByLectureId(id);
        lectureNotificationConfigRepository.deleteByLectureId(id);
        lectureRepository.deleteById(id);
    }
}
