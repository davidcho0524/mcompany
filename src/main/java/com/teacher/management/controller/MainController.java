package com.teacher.management.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@lombok.RequiredArgsConstructor
public class MainController {

    private final com.teacher.management.repository.LectureRepository lectureRepository;
    private final com.teacher.management.repository.CustomerRepository customerRepository;

    @GetMapping("/")
    public String index(org.springframework.ui.Model model) {
        long ongoingLecturesCount = lectureRepository.countByStatus("SCHEDULED");
        long totalLecturesCount = lectureRepository.count();
        long totalCustomersCount = customerRepository.count();

        model.addAttribute("ongoingLecturesCount", ongoingLecturesCount);
        model.addAttribute("totalLecturesCount", totalLecturesCount);
        model.addAttribute("totalCustomersCount", totalCustomersCount);

        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/api/calendar-events")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.List<java.util.Map<String, Object>> getCalendarEvents() {
        java.util.List<java.util.Map<String, Object>> events = new java.util.ArrayList<>();
        java.util.List<com.teacher.management.entity.Lecture> lectures = lectureRepository.findAll();

        java.time.format.DateTimeFormatter timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");

        for (com.teacher.management.entity.Lecture lecture : lectures) {
            java.util.Map<String, Object> event = new java.util.HashMap<>();
            event.put("id", lecture.getId());

            String title = lecture.getTitle();
            if (lecture.getLectureAt() != null) {
                event.put("start", lecture.getLectureAt().toString());
                String timeStr = lecture.getLectureAt().format(timeFormatter);
                String companyName = lecture.getCompany() != null ? lecture.getCompany().getName() : "-";
                title = "[" + timeStr + "][" + companyName + "] " + title;
            }
            event.put("title", title);
            // Assuming 1 hour duration if not specified, or use totalHours if available
            // (converted to duration)
            // For simplicity, let's just use start time. FullCalendar defaults to 1h.
            // Color coding based on status
            if ("COMPLETED".equals(lecture.getStatus())) {
                event.put("color", "#28a745"); // Green
            } else if ("CANCELLED".equals(lecture.getStatus())) {
                event.put("color", "#dc3545"); // Red
            } else {
                event.put("color", "#007bff"); // Blue (Scheduled)
            }

            // Add extended props if needed
            java.util.Map<String, Object> extendedProps = new java.util.HashMap<>();
            extendedProps.put("company", lecture.getCompany() != null ? lecture.getCompany().getName() : "");
            extendedProps.put("customer", lecture.getCustomer() != null ? lecture.getCustomer().getName() : "");
            event.put("extendedProps", extendedProps);

            events.add(event);
        }
        return events;
    }
}
