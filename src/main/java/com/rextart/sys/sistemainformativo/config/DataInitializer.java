package com.rextart.sys.sistemainformativo.config;

import com.rextart.sys.sistemainformativo.model.User;
import com.rextart.sys.sistemainformativo.model.UserRole;
import com.rextart.sys.sistemainformativo.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${password.admin}")
    private String password;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@rextart.com");
            admin.setPasswordHash(passwordEncoder.encode(password));
            admin.setFirstName("Admin");
            admin.setLastName("Rextart");
            admin.setRole(UserRole.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);
        }
    }
}
