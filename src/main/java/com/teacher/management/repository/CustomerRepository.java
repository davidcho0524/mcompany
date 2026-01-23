package com.teacher.management.repository;

import com.teacher.management.dto.DropdownDto;
import com.teacher.management.entity.Customer;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<DropdownDto> findAllByOrderByNameAsc();

    @Override
    @EntityGraph(attributePaths = { "company" })
    org.springframework.data.domain.Page<Customer> findAll(org.springframework.data.domain.Pageable pageable);

    @Override
    @EntityGraph(attributePaths = { "company" })
    java.util.Optional<Customer> findById(Long id);
}
