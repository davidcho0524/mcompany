package com.teacher.management.service;

import com.teacher.management.entity.Lecture;
import com.teacher.management.entity.LectureNotificationConfig;
import com.teacher.management.entity.NotificationTemplate;
import com.teacher.management.repository.LectureNotificationConfigRepository;
import com.teacher.management.repository.LectureRepository;
import com.teacher.management.repository.NotificationLogRepository;
import com.teacher.management.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationTemplateRepository templateRepository;
    private final LectureNotificationConfigRepository configRepository;
    private final LectureRepository lectureRepository;
    private final SmsService smsService;
    private final NotificationLogRepository logRepository;

    // --- Template Methods ---

    public List<NotificationTemplate> getAllTemplates() {
        return templateRepository.findAll();
    }

    public Page<NotificationTemplate> getTemplates(Pageable pageable) {
        return templateRepository.findAll(pageable);
    }

    public NotificationTemplate getTemplate(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid template ID"));
    }

    @Transactional
    public void saveTemplate(NotificationTemplate template) {
        templateRepository.save(template);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        templateRepository.deleteById(id);
    }

    // --- Mapping Methods ---

    public List<LectureNotificationConfig> getConfigsByLecture(Long lectureId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid lecture ID"));
        return configRepository.findByLecture(lecture);
    }

    @Transactional
    public void saveConfig(Long lectureId, Long templateId, String timingType) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid lecture ID"));

        NotificationTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid template ID"));

        // Check if exists
        Optional<LectureNotificationConfig> existing = configRepository.findByLectureAndTimingType(lecture, timingType);

        LectureNotificationConfig config = existing.orElse(new LectureNotificationConfig());
        config.setLecture(lecture);
        config.setTemplate(template);
        config.setTimingType(timingType);

        configRepository.save(config);
    }

    @Transactional
    public void deleteConfig(Long configId) {
        configRepository.deleteById(configId);
    }

    @Transactional
    public void deleteConfigByLectureAndTimingType(Long lectureId, String timingType) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid lecture ID"));
        Optional<LectureNotificationConfig> existing = configRepository.findByLectureAndTimingType(lecture, timingType);
        existing.ifPresent(config -> configRepository.delete(config));
    }

    @Transactional
    public void sendImmediateNotification(Long lectureId, Long templateId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid lecture ID"));

        NotificationTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid template ID"));

        if (lecture.getCustomer() == null) {
            throw new IllegalStateException("No customer assigned to this lecture");
        }

        String customerPhone = lecture.getCustomer().getPhone();
        if (customerPhone == null || customerPhone.isEmpty()) {
            throw new IllegalStateException("Customer phone number is missing");
        }
        String formattedPhone = customerPhone.replaceAll("[^0-9]", "");

        // Replace placeholders associated with the lecture
        String content = buildMessageContent(lecture, template);

        try {
            String messageId = smsService.sendMessage(formattedPhone, content, template.getMessageType(),
                    template.getKakaoPfId(), template.getKakaoTemplateId());

            // Log success
            com.teacher.management.entity.NotificationLog logEntry = new com.teacher.management.entity.NotificationLog();
            logEntry.setLecture(lecture);
            logEntry.setNotificationType("MANUAL"); // Manual Trigger
            logEntry.setStatus("SUCCESS");
            logEntry.setMessageId(messageId);
            logEntry.setMessageType(template.getMessageType());
            logEntry.setMemberId(lecture.getCustomer().getId());
            logEntry.setSentAt(java.time.LocalDateTime.now());
            logRepository.save(logEntry);

        } catch (Exception e) {
            // Log failure
            com.teacher.management.entity.NotificationLog logEntry = new com.teacher.management.entity.NotificationLog();
            logEntry.setLecture(lecture);
            logEntry.setNotificationType("MANUAL");
            logEntry.setStatus("FAIL");
            logEntry.setFailReason(e.getMessage());
            logEntry.setMessageType(template.getMessageType());
            logEntry.setMemberId(lecture.getCustomer().getId());
            logEntry.setSentAt(java.time.LocalDateTime.now());
            logRepository.save(logEntry);
            throw new RuntimeException("Failed to send notification: " + e.getMessage());
        }
    }
    // --- Log Methods ---

    @Transactional
    public void sendTemplateByName(Lecture lecture, String templateName) {
        if (lecture.getCustomer() == null) {
            log.warn("Cannot send {} template: Lecture {} has no customer assigned", templateName, lecture.getId());
            return;
        }

        String customerPhone = lecture.getCustomer().getPhone();
        if (customerPhone == null || customerPhone.isEmpty()) {
            log.warn("Cannot send {} template: Lecture {} customer phone is missing", templateName, lecture.getId());
            return;
        }
        String formattedPhone = customerPhone.replaceAll("[^0-9]", "");

        NotificationTemplate template = templateRepository.findByName(templateName)
                .orElse(null);

        if (template == null) {
            log.warn("Cannot send template: Template with name '{}' not found", templateName);
            return;
        }

        String content = buildMessageContent(lecture, template);

        try {
            String messageId = smsService.sendMessage(formattedPhone, content, template.getMessageType(),
                    template.getKakaoPfId(), template.getKakaoTemplateId());

            com.teacher.management.entity.NotificationLog logEntry = new com.teacher.management.entity.NotificationLog();
            logEntry.setLecture(lecture);
            logEntry.setNotificationType("SYSTEM_TRIGGER");
            logEntry.setStatus("SUCCESS");
            logEntry.setMessageId(messageId);
            logEntry.setMessageType(template.getMessageType());
            logEntry.setMemberId(lecture.getCustomer().getId());
            logEntry.setSentAt(java.time.LocalDateTime.now());
            logRepository.save(logEntry);

            log.info("Successfully sent {} notification for lecture {}", templateName, lecture.getId());
        } catch (Exception e) {
            log.error("Failed to send {} notification for lecture {}", templateName, lecture.getId(), e);
            com.teacher.management.entity.NotificationLog logEntry = new com.teacher.management.entity.NotificationLog();
            logEntry.setLecture(lecture);
            logEntry.setNotificationType("SYSTEM_TRIGGER");
            logEntry.setStatus("FAIL");
            logEntry.setFailReason(e.getMessage());
            logEntry.setMessageType(template.getMessageType());
            logEntry.setMemberId(lecture.getCustomer().getId());
            logEntry.setSentAt(java.time.LocalDateTime.now());
            logRepository.save(logEntry);
        }
    }

    private String buildMessageContent(Lecture lecture, NotificationTemplate template) {
        String content = template.getContent();
        if (content == null)
            return "";

        return content.replace("{title}", lecture.getTitle() != null ? lecture.getTitle() : "")
                .replace("{date}", lecture.getLectureAt() != null
                        ? lecture.getLectureAt()
                                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
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
    }

    public Page<com.teacher.management.entity.NotificationLog> getNotificationLogs(Pageable pageable) {
        return logRepository.findAll(pageable);
    }
}
