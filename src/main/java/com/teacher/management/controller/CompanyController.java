package com.teacher.management.controller;

import com.teacher.management.entity.Company;
import com.teacher.management.service.CompanyService;
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
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public String list(Model model,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        model.addAttribute("companies", companyService.getAllCompanies(pageable));
        return "company/list";
    }

    @GetMapping("/new")
    public String form(Model model, @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        model.addAttribute("company", new Company());
        if ("XMLHttpRequest".equals(requestedWith)) {
            return "company/form :: formFragment";
        }
        return "company/form";
    }

    @PostMapping
    public String save(Company company) {
        companyService.saveCompany(company);
        return "redirect:/companies";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        model.addAttribute("company", companyService.getCompanyById(id));
        if ("XMLHttpRequest".equals(requestedWith)) {
            return "company/form :: formFragment";
        }
        return "company/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, Company company) {
        // Ensure the ID is set for update
        company.setId(id);
        companyService.saveCompany(company);
        return "redirect:/companies";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return "redirect:/companies";
    }
}
