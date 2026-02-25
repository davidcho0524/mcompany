package com.teacher.management.controller;

import com.teacher.management.entity.Lecture;
import com.teacher.management.service.CompanyService;
import com.teacher.management.service.CustomerService;
import com.teacher.management.service.LectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/lectures")
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;
    private final CompanyService companyService;
    private final CustomerService customerService;
    private final com.teacher.management.service.NotificationService notificationService;

    @GetMapping
    public String list(Model model,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        model.addAttribute("lectures", lectureService.getAllLectures(pageable));
        return "lecture/list";
    }

    @GetMapping("/new")
    public String form(Model model, @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        model.addAttribute("lecture", new Lecture());
        model.addAttribute("categories", com.teacher.management.entity.LectureCategory.values());
        model.addAttribute("companies", companyService.getAllCompaniesForDropdown());
        model.addAttribute("customers", java.util.Collections.emptyList());
        if ("XMLHttpRequest".equals(requestedWith)) {
            return "lecture/form :: formFragment";
        }
        return "lecture/form";
    }

    @PostMapping
    public String save(Lecture lecture,
            @org.springframework.web.bind.annotation.RequestParam(value = "sendConfirmation", defaultValue = "false") boolean sendConfirmation) {
        lectureService.saveLecture(lecture);
        if (sendConfirmation && "CONFIRMED".equals(lecture.getStatus())) {
            notificationService.sendTemplateByName(lecture, "강의 확정 문자");
        }
        return "redirect:/lectures";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        Lecture lecture = lectureService.getLectureById(id);
        model.addAttribute("lecture", lecture);
        model.addAttribute("categories", com.teacher.management.entity.LectureCategory.values());
        model.addAttribute("companies", companyService.getAllCompaniesForDropdown());

        if (lecture.getCompany() != null) {
            model.addAttribute("customers", customerService.getCustomersByCompanyId(lecture.getCompany().getId()));
        } else {
            model.addAttribute("customers", java.util.Collections.emptyList());
        }

        if ("XMLHttpRequest".equals(requestedWith)) {
            return "lecture/form :: formFragment";
        }
        return "lecture/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, Lecture lecture,
            @org.springframework.web.bind.annotation.RequestParam(value = "sendConfirmation", defaultValue = "false") boolean sendConfirmation) {
        Lecture existingLecture = lectureService.getLectureById(id);
        existingLecture.setTitle(lecture.getTitle());
        existingLecture.setCategory(lecture.getCategory());
        existingLecture.setPrice(lecture.getPrice());
        existingLecture.setTotalHours(lecture.getTotalHours());
        existingLecture.setLectureAt(lecture.getLectureAt());
        existingLecture.setIsPaid(lecture.getIsPaid());
        existingLecture.setStatus(lecture.getStatus());
        existingLecture.setCompany(lecture.getCompany());
        existingLecture.setCustomer(lecture.getCustomer());
        existingLecture.setLectureType(lecture.getLectureType());
        existingLecture.setLocation(lecture.getLocation());
        existingLecture.setAttendeeCount(lecture.getAttendeeCount());
        existingLecture.setNotificationYn(lecture.getNotificationYn() != null ? lecture.getNotificationYn() : "N");

        lectureService.saveLecture(existingLecture);

        if (sendConfirmation && "CONFIRMED".equals(existingLecture.getStatus())) {
            notificationService.sendTemplateByName(existingLecture, "강의 확정 문자");
        }

        return "redirect:/lectures";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        lectureService.deleteLecture(id);
        return "redirect:/lectures";
    }

    @GetMapping("/{id}/feedback")
    public String getFeedbackForm(@PathVariable Long id, Model model) {
        Lecture lecture = lectureService.getLectureById(id);
        model.addAttribute("lecture", lecture);
        return "lecture/feedback_modal :: feedbackFormFragment";
    }

    @PostMapping("/{id}/feedback")
    public String saveFeedback(@PathVariable Long id, Lecture lecture) {
        Lecture existingLecture = lectureService.getLectureById(id);
        existingLecture.setLectureMaterials(lecture.getLectureMaterials());
        existingLecture.setDownloadLink(lecture.getDownloadLink());
        existingLecture.setCustomerEvaluation(lecture.getCustomerEvaluation());
        existingLecture.setSelfReviewContent(lecture.getSelfReviewContent());
        existingLecture.setSelfReviewSpeed(lecture.getSelfReviewSpeed());
        existingLecture.setSelfReviewDelivery(lecture.getSelfReviewDelivery());
        existingLecture.setMentorFeedback(lecture.getMentorFeedback());
        existingLecture.setImprovements(lecture.getImprovements());
        existingLecture.setLinkedinPost(lecture.getLinkedinPost());
        existingLecture.setNaverBlogPost(lecture.getNaverBlogPost());
        existingLecture.setWordpressPost(lecture.getWordpressPost());

        lectureService.saveLecture(existingLecture);
        return "redirect:/lectures";
    }
}
