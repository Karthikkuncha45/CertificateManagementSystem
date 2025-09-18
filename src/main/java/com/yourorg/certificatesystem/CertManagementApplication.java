package com.yourorg.certificatesystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.yourorg.certificatesystem.entity.User;
import com.yourorg.certificatesystem.repository.UserRepository;

@SpringBootApplication
public class CertManagementApplication implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(CertManagementApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed admin user
        if (!userRepository.existsByRollNumber("ADMIN001")) {
            User admin = new User();
            admin.setFullName("System Administrator");
            admin.setRollNumber("ADMIN001");
            admin.setEmail("admin@college.edu");
            admin.setPhoneNumber("9999999999");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setStudyingYear(4);
            admin.setBranch("CSE");
            admin.setSection("A");
            admin.setEnabled(true);
            userRepository.save(admin);
            System.out.println("Admin user created: ADMIN001 / admin123");
        }
    }
}