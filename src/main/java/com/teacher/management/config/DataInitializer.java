package com.teacher.management.config;

import com.teacher.management.entity.Admin;
import com.teacher.management.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Admin account handling
        Admin admin = adminRepository.findByUsername("admin").orElse(new Admin());
        if (admin.getId() == null) {
            admin.setUsername("admin");
            admin.setCreatedAt(LocalDateTime.now());
            admin.setName("Administrator");
            admin.setRole("ROLE_ADMIN");
        }
        admin.setPassword(passwordEncoder.encode("admin1!"));
        admin.setUpdatedAt(LocalDateTime.now());
        adminRepository.save(admin);
        System.out.println("Updated/Created user: admin / admin1!");

        // M3 Company account handling
        Admin m3company = adminRepository.findByUsername("m3company").orElse(new Admin());
        if (m3company.getId() == null) {
            m3company.setUsername("m3company");
            m3company.setCreatedAt(LocalDateTime.now());
            m3company.setName("M3 Company");
            m3company.setRole("ROLE_ADMIN");
        }
        m3company.setPassword(passwordEncoder.encode("m3company1!"));
        m3company.setUpdatedAt(LocalDateTime.now());
        adminRepository.save(m3company);
        System.out.println("Updated/Created user: m3company / m3company1!");
    }
}
