package com.teacher.management.service;

import com.teacher.management.entity.Company;
import com.teacher.management.dto.DropdownDto;
import com.teacher.management.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;

    public List<DropdownDto> getAllCompaniesForDropdown() {
        return companyRepository.findAllByOrderByNameAsc();
    }

    @Transactional
    public Company findOrCreateCompany(Company companyInput) {
        if (companyInput.getName() == null || companyInput.getName().trim().isEmpty()) {
            return null;
        }

        Company existingCompany = companyRepository.findByName(companyInput.getName());
        if (existingCompany != null) {
            // Update existing company details if provided (optional, but requested in plan)
            if (companyInput.getPhone() != null && !companyInput.getPhone().isEmpty()) {
                existingCompany.setPhone(companyInput.getPhone());
            }
            if (companyInput.getContact() != null && !companyInput.getContact().isEmpty()) {
                existingCompany.setContact(companyInput.getContact());
            }
            if (companyInput.getRegistrationNumber() != null && !companyInput.getRegistrationNumber().isEmpty()) {
                existingCompany.setRegistrationNumber(companyInput.getRegistrationNumber());
            }
            if (companyInput.getEmail() != null && !companyInput.getEmail().isEmpty()) {
                existingCompany.setEmail(companyInput.getEmail());
            }
            if (companyInput.getType() != null) {
                existingCompany.setType(companyInput.getType());
            }
            return existingCompany;
        } else {
            // Create new company
            return companyRepository.save(companyInput);
        }
    }
}
