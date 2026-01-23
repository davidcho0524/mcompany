package com.teacher.management.service;

import com.teacher.management.dto.DropdownDto;

import com.teacher.management.entity.Company;
import com.teacher.management.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;

    public Page<Company> getAllCompanies(Pageable pageable) {
        return companyRepository.findAll(pageable);
    }

    public List<DropdownDto> getAllCompaniesForDropdown() {
        return companyRepository.findAllByOrderByNameAsc();
    }

    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid company Id:" + id));
    }

    @Transactional
    public void saveCompany(Company company) {
        companyRepository.save(company);
    }

    @Transactional
    public void deleteCompany(Long id) {
        companyRepository.deleteById(id);
    }
}
