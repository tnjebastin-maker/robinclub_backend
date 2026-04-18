package com.example.e_commerce.controller;

import com.example.e_commerce.entity.*;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final UserRepository userRepository;

    private User getUser(User principal) {
        return userRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public ResponseEntity<Cart> getCart(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cartService.getCart(getUser(user)));
    }

    @PostMapping("/add")
    public ResponseEntity<Cart> addItem(@AuthenticationPrincipal User user,
                                         @RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("productId").toString());
        int quantity = body.get("quantity") != null
                ? Integer.parseInt(body.get("quantity").toString()) : 1;
        String selectedImage = body.get("selectedImage") != null ? body.get("selectedImage").toString() : null;
        return ResponseEntity.ok(cartService.addItem(getUser(user), productId, quantity, selectedImage));
    }

    @PutMapping("/update/{itemId}")
    public ResponseEntity<Cart> updateItem(@AuthenticationPrincipal User user,
                                            @PathVariable Long itemId,
                                            @RequestBody Map<String, Object> body) {
        int quantity = Integer.parseInt(body.get("quantity").toString());
        return ResponseEntity.ok(cartService.updateItem(getUser(user), itemId, quantity));
    }

    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<Cart> removeItem(@AuthenticationPrincipal User user,
                                            @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(getUser(user), itemId));
    }
}
