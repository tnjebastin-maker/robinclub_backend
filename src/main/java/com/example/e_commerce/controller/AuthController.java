package com.example.e_commerce.controller;

import com.example.e_commerce.dto.AuthRequest;
import com.example.e_commerce.dto.AuthResponse;
import com.example.e_commerce.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    // Called with Google ID token (credential) from @react-oauth/google
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody Map<String, String> body) throws Exception {
        return ResponseEntity.ok(authService.googleLogin(body.get("token")));
    }

    // Called with Google userinfo (access_token flow) from useGoogleLogin hook
    @PostMapping("/google/token")
    public ResponseEntity<AuthResponse> googleTokenLogin(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(authService.googleTokenLogin(
                body.get("googleId"), body.get("email"), body.get("name"), body.get("picture")));
    }
}
