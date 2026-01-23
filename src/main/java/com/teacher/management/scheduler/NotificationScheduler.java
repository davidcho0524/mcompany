package com.teacher.management.scheduler;

import com.teacher.management.entity.Lecture;
import com.teacher.management.entity.NotificationLog;
import com.teacher.management.repository.LectureRepository;
import com.teacher.management.repository.NotificationLogRepository;
import com.teacher.management.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final LectureRepository lectureRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final SmsService smsService;

    @Scheduled(cron = "0 */10 * * * *") // Every 10 minutes
    public void scheduleNotifications() {
        LocalDateTime now = LocalDateTime.now();

        // 1 Day Before
        processNotifications(now, 24, "1DAY");

        // 1 Hour Before
        processNotifications(now, 1, "1HOUR");
    }

    private void processNotifications(LocalDateTime now, int hoursAhead, String type) {
        // Target: lectureAt ~= now + hoursAhead
        // Search range +/- 30m to cover scheduler interval
        LocalDateTime targetTime = now.plusHours(hoursAhead);
        LocalDateTime start = targetTime.minusMinutes(30);
        LocalDateTime end = targetTime.plusMinutes(30);

        List<Lecture> lectures = lectureRepository.findByNotificationYnAndLectureAtBetween("Y", start, end);

        if (!lectures.isEmpty()) {
            log.info("Found {} lectures for {} notification check", lectures.size(), type);
        }

        for (Lecture lecture : lectures) {
            try {
                if (!notificationLogRepository.existsByLectureAndNotificationType(lecture, type)) {
                    sendNotification(lecture, type);
                }
            } catch (Exception e) {
                log.error("Error processing lecture ID {} for notification {}", lecture.getId(), type, e);
            }
        }
    }

    private void sendNotification(Lecture lecture, String type) {
        String customerPhone = lecture.getCustomer().getPhone();
        if (customerPhone == null || customerPhone.isEmpty()) {
            log.warn("Customer phone missing for lecture {}", lecture.getId());
            return;
        }

        String formattedPhone = customerPhone.replaceAll("[^0-9]", "");
        String message = buildMessage(lecture, type);

        try {
            String messageId = smsService.sendSms(formattedPhone, message);

            NotificationLog logEntry = new NotificationLog();
            logEntry.setLecture(lecture);
            logEntry.setNotificationType(type);
            logEntry.setStatus("SUCCESS");
            logEntry.setMessageId(messageId);
            logEntry.setSentAt(LocalDateTime.now());
            notificationLogRepository.save(logEntry);

            log.info("Sent {} notification to {} for lecture {}", type, formattedPhone, lecture.getId());
        } catch (Exception e) {
            log.error("Failed to send sms", e);
            NotificationLog logEntry = new NotificationLog();
            logEntry.setLecture(lecture);
            logEntry.setNotificationType(type);
            logEntry.setStatus("FAIL");
            logEntry.setFailReason(e.getMessage());
            logEntry.setSentAt(LocalDateTime.now());
            notificationLogRepository.save(logEntry);
        }
    }

    private String buildMessage(Lecture lecture, String type) {
        String timeDesc = type.equals("1DAY") ? "1일 전" : "1시간 전";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M월 d일 H시 m분");
        String formattedDate = lecture.getLectureAt().format(formatter);

        return String.format("[강의 알림] %s\n(%s)\n\n안녕하세요. 신청하신 강의가 시작 %s입니다.\n일시: %s\n",
                lecture.getTitle(),
                timeDesc,
                timeDesc,
                formattedDate);
    }
}
