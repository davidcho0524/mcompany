package com.teacher.management.controller;

import com.teacher.management.entity.Customer;
import com.teacher.management.service.CompanyService;
import com.teacher.management.service.CustomerService;
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
@RequestMapping("/customers")
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class CustomerController {

    private final CustomerService customerService;
    private final CompanyService companyService;

    @GetMapping
    public String list(Model model,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        model.addAttribute("customers", customerService.getAllCustomers(pageable));
        return "customer/list";
    }

    @GetMapping("/new")
    public String form(Model model, @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        model.addAttribute("customer", new Customer());
        model.addAttribute("companies", companyService.getAllCompaniesForDropdown());
        if ("XMLHttpRequest".equals(requestedWith)) {
            return "customer/form :: formFragment";
        }
        return "customer/form";
    }

    @PostMapping
    public String save(Customer customer) {
        customerService.saveCustomer(customer);
        return "redirect:/customers";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        long start = System.currentTimeMillis();

        Customer customer = customerService.getCustomerById(id);
        long afterCustomer = System.currentTimeMillis();
        log.info("Fetching customer took: {} ms", (afterCustomer - start));

        model.addAttribute("customer", customer);

        var companies = companyService.getAllCompaniesForDropdown();
        long afterCompanies = System.currentTimeMillis();
        log.info("Fetching companies dropdown took: {} ms", (afterCompanies - afterCustomer));

        model.addAttribute("companies", companies);

        if ("XMLHttpRequest".equals(requestedWith)) {
            return "customer/form :: formFragment";
        }
        return "customer/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, Customer customer) {
        customer.setId(id);
        customerService.saveCustomer(customer);
        return "redirect:/customers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return "redirect:/customers";
    }
}
