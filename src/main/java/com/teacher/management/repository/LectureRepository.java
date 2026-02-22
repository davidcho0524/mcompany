package com.teacher.management.repository;

import com.teacher.management.entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
    @Override
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "customer", "company" })
    org.springframework.data.domain.Page<Lecture> findAll(org.springframework.data.domain.Pageable pageable);

    @Override
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "customer", "company" })
    java.util.Optional<Lecture> findById(Long id);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "customer", "company" })
    List<Lecture> findByNotificationYnAndLectureAtBetween(String notificationYn, LocalDateTime start,
            LocalDateTime end);

    long countByStatus(String status);

    List<Lecture> findByCustomer(com.teacher.management.entity.Customer customer);
}
