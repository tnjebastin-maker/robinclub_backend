package com.example.e_commerce.controller;

import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;

    private User getUser(User principal) {
        return userRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(getUser(user));
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@AuthenticationPrincipal User user,
                                               @RequestBody Map<String, String> body) {
        User managed = getUser(user);
        if (body.containsKey("name") && !body.get("name").isBlank())
            managed.setName(body.get("name"));
        if (body.containsKey("phone"))
            managed.setPhone(body.get("phone").isBlank() ? null : body.get("phone"));
        userRepository.save(managed);
        return ResponseEntity.ok(managed);
    }
}
