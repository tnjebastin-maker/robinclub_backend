package com.example.e_commerce.controller;

import com.example.e_commerce.entity.Payment;
import com.example.e_commerce.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    // Step 1: Create Razorpay order and return order details to frontend
    @PostMapping("/create/{orderId}")
    public ResponseEntity<Map<String, Object>> createPayment(@PathVariable Long orderId) throws Exception {
        return ResponseEntity.ok(paymentService.createRazorpayOrder(orderId));
    }

    // Step 2: Verify payment signature after user completes payment
    @PostMapping("/verify")
    public ResponseEntity<Payment> verifyPayment(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(paymentService.verifyAndSave(
                body.get("razorpayOrderId"),
                body.get("razorpayPaymentId"),
                body.get("razorpaySignature")
        ));
    }
}
