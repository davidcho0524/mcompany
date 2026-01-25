package com.teacher.management.scheduler;

import com.teacher.management.entity.Lecture;
import com.teacher.management.entity.NotificationLog;
import com.teacher.management.entity.LectureNotificationConfig;
import com.teacher.management.entity.NotificationTemplate;
import com.teacher.management.repository.LectureNotificationConfigRepository;
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
    private final LectureNotificationConfigRepository configRepository;
    private final SmsService smsService;

    @Scheduled(cron = "0 */10 * * * *") // Every 10 minutes
    public void scheduleNotifications() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Notification scheduler running at {}", now);

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
                if (!notificationLogRepository.existsByLectureAndNotificationTypeAndStatus(lecture, type, "SUCCESS")) {
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

        // Find configuration for this lecture and type
        LectureNotificationConfig config = configRepository.findByLectureAndTimingType(lecture, type)
                .orElse(null);

        if (config == null) {
            log.info("No notification config found for lecture {} type {}", lecture.getId(), type);
            return;
        }

        NotificationTemplate template = config.getTemplate();
        if (template == null) {
            log.warn("Config exists but no template for lecture {} type {}", lecture.getId(), type);
            return;
        }

        String formattedPhone = customerPhone.replaceAll("[^0-9]", "");

        // Build message content from template
        String content = buildMessageFromTemplate(lecture, template);

        try {
            String messageId = smsService.sendMessage(formattedPhone, content, template.getMessageType(),
                    template.getKakaoPfId(), template.getKakaoTemplateId());

            NotificationLog logEntry = new NotificationLog();
            logEntry.setLecture(lecture);
            logEntry.setNotificationType(type);
            logEntry.setStatus("SUCCESS");
            logEntry.setMessageId(messageId);
            logEntry.setMessageType(template.getMessageType());
            logEntry.setMemberId(lecture.getCustomer().getId());
            logEntry.setSentAt(LocalDateTime.now());
            notificationLogRepository.save(logEntry);

            log.info("Sent {} notification ({}) to {} for lecture {}", type, template.getMessageType(), formattedPhone,
                    lecture.getId());
        } catch (Exception e) {
            log.error("Failed to send notification", e);
            NotificationLog logEntry = new NotificationLog();
            logEntry.setLecture(lecture);
            logEntry.setNotificationType(type);
            logEntry.setStatus("FAIL");
            logEntry.setFailReason(e.getMessage());
            logEntry.setMessageType(template.getMessageType());
            logEntry.setMemberId(lecture.getCustomer().getId());
            logEntry.setSentAt(LocalDateTime.now());
            notificationLogRepository.save(logEntry);
        }
    }

    private String buildMessageFromTemplate(Lecture lecture, NotificationTemplate template) {
        String content = template.getContent();
        if (content == null)
            content = "";

        // Standard replacements
        content = content.replace("{title}", lecture.getTitle() != null ? lecture.getTitle() : "")
                .replace("{date}", lecture.getLectureAt() != null
                        ? lecture.getLectureAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                        : "")
                .replace("{customer}", lecture.getCustomer().getName() != null ? lecture.getCustomer().getName() : "")
                .replace("{company}", lecture.getCompany() != null ? lecture.getCompany().getName() : "");

        return content;
    }
}
