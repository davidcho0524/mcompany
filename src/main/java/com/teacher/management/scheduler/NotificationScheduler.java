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

    @org.springframework.transaction.annotation.Transactional
    @Scheduled(cron = "0 */10 * * * *", zone = "Asia/Seoul") // Every 10 minutes
    public void scheduleNotifications() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Notification scheduler running at {}", now);

        // 3 Days Before - Send at 20:40 for lectures scheduled for 3 days later
        // Run only during the 20:40 - 20:50 window
        if (now.getHour() == 20 && now.getMinute() >= 40) {
            // "오늘" 기준으로 정확히 3일(DAY) 뒤의 00:00:00 ~ 23:59:59 조회
            LocalDateTime triggerDayStart = now.plusDays(3).toLocalDate().atStartOfDay();
            LocalDateTime triggerDayEnd = now.plusDays(3).toLocalDate().atTime(23, 59, 59, 999999999);
            log.info("Running 3 Days Before check for lectures between {} and {}", triggerDayStart, triggerDayEnd);
            processNotifications(triggerDayStart, triggerDayEnd, "3DAY");
        }

        // 3 Hours Before
        // Keep existing logic: Target is now + 3 hours, search window +/- 30 mins
        LocalDateTime targetTime = now.plusHours(3);
        LocalDateTime start = targetTime.minusMinutes(30);
        LocalDateTime end = targetTime.plusMinutes(30);
        processNotifications(start, end, "3HOUR");

        // 1 Hour Before
        // Target is now + 1 hour, search window +/- 30 mins
        LocalDateTime targetTime1h = now.plusHours(1);
        LocalDateTime start1h = targetTime1h.minusMinutes(30);
        LocalDateTime end1h = targetTime1h.plusMinutes(30);
        processNotifications(start1h, end1h, "1HOUR");
    }

    private void processNotifications(LocalDateTime start, LocalDateTime end, String type) {
        List<Lecture> lectures = lectureRepository.findByNotificationYnAndLectureAtBetween("Y", start, end);

        if (!lectures.isEmpty()) {
            log.info("Found {} lectures for {} notification check (Range: {} ~ {})", lectures.size(), type, start, end);
        } else {
            // Log only debug or if needed to avoid clutter, but info is fine for now
            log.debug("No lectures found for {} notification check", type);
        }

        for (Lecture lecture : lectures) {
            try {
                if (lecture.getCustomer() == null) {
                    continue;
                }
                Long memberId = lecture.getCustomer().getId();
                if (!notificationLogRepository.existsByLectureAndNotificationTypeAndStatusAndMemberId(lecture, type,
                        "SUCCESS", memberId)) {
                    sendNotification(lecture, type);
                } else {
                    log.info("Notification {} already sent to customer {} for lecture {}", type, memberId,
                            lecture.getId());
                }
            } catch (Exception e) {
                log.error("Error processing lecture ID {} for notification {}", lecture.getId(), type, e);
            }
        }
    }

    private void sendNotification(Lecture lecture, String type) {
        log.info("Starting sendNotification for lecture_id={}, type={}", lecture.getId(), type);

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

        log.info("Found notification config for lecture {}, configId={}", lecture.getId(), config.getId());

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
            logEntry.setOriginalLectureName(lecture.getTitle());
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
            logEntry.setOriginalLectureName(lecture.getTitle());
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
                .replace("{company}", lecture.getCompany() != null ? lecture.getCompany().getName() : "")
                .replace("{price}",
                        lecture.getPrice() != null ? java.text.NumberFormat.getInstance().format(lecture.getPrice())
                                : "0")
                .replace("{totalHours}",
                        lecture.getTotalHours() != null ? String.valueOf(lecture.getTotalHours()) : "0")
                .replace("{category}", lecture.getCategory() != null ? lecture.getCategory().getDescription() : "")
                .replace("{status}", lecture.getStatus() != null ? lecture.getStatus() : "")
                .replace("{lectureType}", lecture.getLectureType() != null ? lecture.getLectureType() : "")
                .replace("{location}", lecture.getLocation() != null ? lecture.getLocation() : "")
                .replace("{attendeeCount}",
                        lecture.getAttendeeCount() != null ? String.valueOf(lecture.getAttendeeCount()) : "0");

        return content;
    }
}
