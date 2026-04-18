package com.example.e_commerce.config;

import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    // Set to true ONLY on first run to wipe all data, then set back to false
    private static final boolean RESET_DB = false;

    @Override
    @Transactional
    public void run(String... args) {
        if (RESET_DB) {
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE payments").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE order_items").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE orders").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE cart_items").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE carts").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE products").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE users").executeUpdate();
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
            System.out.println("🗑️  All tables cleared.");
        }

        if (!userRepository.existsByEmail("tnjebastin@gmail.com")) {
            User admin = new User();
            admin.setEmail("tnjebastin@gmail.com");
            admin.setName("Admin");
            admin.setPhone(null);
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
            System.out.println("✅ Admin created: tnjebastin@gmail.com / Admin@123");
        }
    }
}
