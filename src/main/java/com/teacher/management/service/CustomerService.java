package com.teacher.management.service;

import com.teacher.management.dto.DropdownDto;

import com.teacher.management.entity.Customer;
import com.teacher.management.repository.CustomerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final com.teacher.management.repository.LectureRepository lectureRepository;

    public Page<Customer> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    public List<DropdownDto> getAllCustomersForDropdown() {
        return customerRepository.findAllByOrderByNameAsc();
    }

    public List<DropdownDto> getCustomersByCompanyId(Long companyId) {
        return customerRepository.findAllByCompanyIdOrderByNameAsc(companyId);
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer Id:" + id));
    }

    @Transactional
    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = getCustomerById(id);

        // Unlink associated lectures
        List<com.teacher.management.entity.Lecture> lectures = lectureRepository.findByCustomer(customer);
        for (com.teacher.management.entity.Lecture lecture : lectures) {
            lecture.setCustomer(null);
            lectureRepository.save(lecture);
        }

        customerRepository.deleteById(id);
    }
}
