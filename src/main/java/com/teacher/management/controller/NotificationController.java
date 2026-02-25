package com.teacher.management.controller;

import com.teacher.management.entity.Lecture;
import com.teacher.management.entity.LectureNotificationConfig;
import com.teacher.management.entity.NotificationTemplate;
import com.teacher.management.service.LectureService;
import com.teacher.management.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final LectureService lectureService;

    // --- Template Management ---

    @GetMapping("/templates")
    public String listTemplates(Model model,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<NotificationTemplate> templates = notificationService.getTemplates(pageable);
        model.addAttribute("templates", templates);
        return "notifications/template_list";
    }

    @GetMapping("/templates/new")
    public String newTemplateForm(Model model,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        model.addAttribute("template", new NotificationTemplate());
        if ("XMLHttpRequest".equals(requestedWith)) {
            return "notifications/template_form :: formFragment";
        }
        return "notifications/template_form";
    }

    @PostMapping("/templates/save")
    public String saveTemplate(@ModelAttribute NotificationTemplate template) {
        if (template.getContent() == null) {
            template.setContent("");
        }
        notificationService.saveTemplate(template);
        return "redirect:/notifications/templates";
    }

    @GetMapping("/templates/{id}")
    public String editTemplateForm(@PathVariable Long id, Model model,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        model.addAttribute("template", notificationService.getTemplate(id));
        if ("XMLHttpRequest".equals(requestedWith)) {
            return "notifications/template_form :: formFragment";
        }
        return "notifications/template_form";
    }

    @PostMapping("/templates/{id}/delete")
    public String deleteTemplate(@PathVariable Long id) {
        notificationService.deleteTemplate(id);
        return "redirect:/notifications/templates";
    }

    // --- Mapping Management ---

    @GetMapping("/mappings/{lectureId}")
    public String mapLectureForm(@PathVariable Long lectureId, Model model) {
        Lecture lecture = lectureService.getLectureById(lectureId);
        List<NotificationTemplate> templates = notificationService.getAllTemplates();
        List<LectureNotificationConfig> configs = notificationService.getConfigsByLecture(lectureId);

        model.addAttribute("lecture", lecture);
        model.addAttribute("templates", templates);

        // Find existing template IDs for defaults
        Long dayTemplateId = configs.stream()
                .filter(c -> "3DAY".equals(c.getTimingType()))
                .findFirst()
                .map(c -> c.getTemplate().getId())
                .orElse(null);

        Long hourTemplateId = configs.stream()
                .filter(c -> "3HOUR".equals(c.getTimingType()))
                .findFirst()
                .map(c -> c.getTemplate().getId())
                .orElse(null);

        model.addAttribute("dayTemplateId", dayTemplateId);
        model.addAttribute("hourTemplateId", hourTemplateId);

        return "notifications/mapping_form";
    }

    @PostMapping("/mappings/{lectureId}/save")
    public String saveLectureMapping(@PathVariable Long lectureId,
            @RequestParam(value = "dayTemplateId", required = false) Long dayTemplateId,
            @RequestParam(value = "hourTemplateId", required = false) Long hourTemplateId) {

        if (dayTemplateId != null) {
            notificationService.saveConfig(lectureId, dayTemplateId, "3DAY");
        } else {
            notificationService.deleteConfigByLectureAndTimingType(lectureId, "3DAY");
        }

        if (hourTemplateId != null) {
            notificationService.saveConfig(lectureId, hourTemplateId, "3HOUR");
        } else {
            notificationService.deleteConfigByLectureAndTimingType(lectureId, "3HOUR");
        }

        return "redirect:/lectures";
    }

    @PostMapping("/mappings/{lectureId}/send")
    @ResponseBody
    public org.springframework.http.ResponseEntity<String> sendNotificationNow(@PathVariable Long lectureId,
            @RequestParam("templateId") Long templateId) {
        try {
            notificationService.sendImmediateNotification(lectureId, templateId);
            return org.springframework.http.ResponseEntity.ok("Success");
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.status(500)
                    .body(e.getMessage());
        }
    }
    // --- Log Management ---

    @GetMapping("/logs")
    public String listLogs(Model model,
            @PageableDefault(size = 20, sort = "sentAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<com.teacher.management.entity.NotificationLog> logs = notificationService.getNotificationLogs(pageable);
        model.addAttribute("logs", logs);
        return "notifications/log_list";
    }
}
