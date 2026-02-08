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
        Customer customer = new Customer();
        customer.setCompany(new com.teacher.management.entity.Company());
        model.addAttribute("customer", customer);

        if ("XMLHttpRequest".equals(requestedWith)) {
            return "customer/form :: formFragment";
        }
        return "customer/form";
    }

    @PostMapping
    public String save(Customer customer) {
        if (customer.getCompany() != null) {
            com.teacher.management.entity.Company processedCompany = companyService
                    .findOrCreateCompany(customer.getCompany());
            customer.setCompany(processedCompany);
        }
        customerService.saveCustomer(customer);
        return "redirect:/customers";
    }

    @GetMapping("/by-company/{companyId}")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.List<com.teacher.management.dto.DropdownDto> getCustomersByCompany(@PathVariable Long companyId) {
        return customerService.getCustomersByCompanyId(companyId);
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        long start = System.currentTimeMillis();

        Customer customer = customerService.getCustomerById(id);
        if (customer.getCompany() == null) {
            customer.setCompany(new com.teacher.management.entity.Company());
        }
        long afterCustomer = System.currentTimeMillis();
        log.info("Fetching customer took: {} ms", (afterCustomer - start));

        model.addAttribute("customer", customer);

        if ("XMLHttpRequest".equals(requestedWith)) {
            return "customer/form :: formFragment";
        }
        return "customer/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, Customer customer) {
        customer.setId(id);
        if (customer.getCompany() != null) {
            com.teacher.management.entity.Company processedCompany = companyService
                    .findOrCreateCompany(customer.getCompany());
            customer.setCompany(processedCompany);
        }
        customerService.saveCustomer(customer);
        return "redirect:/customers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return "redirect:/customers";
    }
}
