package com.example.e_commerce.service;

import com.example.e_commerce.dto.AuthRequest;
import com.example.e_commerce.dto.AuthResponse;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.security.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${app.google.client-id}")
    private String googleClientId;

    public AuthResponse register(AuthRequest req) {
        if (userRepository.existsByEmail(req.getEmail()))
            throw new RuntimeException("Email already registered");
        User user = new User();
        user.setEmail(req.getEmail());
        user.setName(req.getName());
        user.setPhone(req.getPhone() != null && !req.getPhone().isBlank() ? req.getPhone() : null);
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(User.Role.USER);
        userRepository.save(user);
        return toResponse(user);
    }

    public AuthResponse login(AuthRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (user.getPassword() == null)
            throw new RuntimeException("Please login with Google");
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword()))
            throw new RuntimeException("Invalid credentials");
        return toResponse(user);
    }

    public AuthResponse googleLogin(String idTokenString) throws Exception {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken == null) throw new RuntimeException("Invalid Google token");
        GoogleIdToken.Payload payload = idToken.getPayload();
        return findOrCreateGoogleUser(
                payload.getSubject(), payload.getEmail(),
                (String) payload.get("name"), (String) payload.get("picture"));
    }

    public AuthResponse googleTokenLogin(String googleId, String email, String name, String picture) {
        return findOrCreateGoogleUser(googleId, email, name, picture);
    }

    private AuthResponse findOrCreateGoogleUser(String googleId, String email, String name, String picture) {
        User user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> userRepository.findByEmail(email).orElseGet(() -> {
                    User u = new User();
                    u.setEmail(email);
                    u.setName(name);
                    u.setGoogleId(googleId);
                    u.setProfilePicture(picture);
                    u.setRole(User.Role.USER);
                    return userRepository.save(u);
                }));

        boolean changed = false;
        if (user.getGoogleId() == null) { user.setGoogleId(googleId); changed = true; }
        if (user.getProfilePicture() == null) { user.setProfilePicture(picture); changed = true; }
        if (user.getRole() == null) { user.setRole(User.Role.USER); changed = true; }
        if (changed) userRepository.save(user);

        return toResponse(user);
    }

    private AuthResponse toResponse(User user) {
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getEmail(), user.getName(), user.getRole().name(), user.getPhone());
    }
}
