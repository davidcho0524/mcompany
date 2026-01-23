package com.teacher.management.repository;

import com.teacher.management.dto.DropdownDto;
import com.teacher.management.entity.Company;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    List<DropdownDto> findAllByOrderByNameAsc();
}
