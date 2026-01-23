package com.teacher.management.config;

import com.teacher.management.entity.Company;
import com.teacher.management.entity.CompanyStatus;
import com.teacher.management.entity.Customer;
import com.teacher.management.entity.Lecture;
import com.teacher.management.repository.CompanyRepository;
import com.teacher.management.repository.CustomerRepository;
import com.teacher.management.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DataInit implements CommandLineRunner {

    private final CompanyRepository companyRepository;
    private final CustomerRepository customerRepository;
    private final LectureRepository lectureRepository;

    @Override
    public void run(String... args) throws Exception {
        generateCompanies();
        generateCustomers();
        generateLectures();
    }

    private void generateCompanies() {
        if (companyRepository.count() < 10) {
            for (int i = 1; i <= 10; i++) {
                Company company = new Company();
                company.setName("Sample Company " + i);
                company.setRegistrationNumber("DUMMY-REG-" + String.format("%03d", i));
                company.setType("PRIVATE");
                company.setContact("Manager " + i);
                company.setPhone("010-0000-" + String.format("%04d", i));
                company.setEmail("contact" + i + "@sample.com");
                company.setAddress("Sample Address " + i);
                company.setStatus(CompanyStatus.ACTIVE);
                company.setCreatedAt(LocalDateTime.now().minusDays(i));

                try {
                    companyRepository.save(company);
                } catch (Exception e) {
                    // Ignore duplicates
                }
            }
            System.out.println("Generated 10 sample companies.");
        }
    }

    private void generateCustomers() {
        if (customerRepository.count() < 20) {
            List<Company> companies = companyRepository.findAll();
            if (companies.isEmpty())
                return;
            Random random = new Random();

            for (int i = 1; i <= 25; i++) {
                Customer customer = new Customer();
                customer.setName("Customer " + i);
                customer.setCompany(companies.get(random.nextInt(companies.size())));
                customer.setPosition("Staff " + i);
                customer.setEmail("customer" + i + "@example.com");
                customer.setPhone("010-1111-" + String.format("%04d", i));
                customer.setBirthDate(LocalDate.of(1990, 1, 1).plusDays(i * 10));
                customer.setCreatedAt(LocalDateTime.now().minusHours(i));

                customerRepository.save(customer);
            }
            System.out.println("Generated 25 sample customers.");
        }
    }

    private void generateLectures() {
        if (lectureRepository.count() < 20) {
            List<Company> companies = companyRepository.findAll();
            List<Customer> customers = customerRepository.findAll();
            if (companies.isEmpty() || customers.isEmpty())
                return;
            Random random = new Random();

            for (int i = 1; i <= 25; i++) {
                Lecture lecture = new Lecture();
                lecture.setTitle("Lecture Subject " + i);
                lecture.setCompany(companies.get(random.nextInt(companies.size())));
                lecture.setCustomer(customers.get(random.nextInt(customers.size())));
                lecture.setCategory("IT/Development");
                lecture.setPrice(new BigDecimal(100000 * i));
                lecture.setIsPaid(i % 2 == 0);
                lecture.setLectureAt(LocalDateTime.now().plusDays(i));
                lecture.setStatus(i % 5 == 0 ? "COMPLETED" : "SCHEDULED");
                lecture.setCreatedAt(LocalDateTime.now().minusMinutes(i * 10));

                lectureRepository.save(lecture);
            }
            System.out.println("Generated 25 sample lectures.");
        }
    }
}
