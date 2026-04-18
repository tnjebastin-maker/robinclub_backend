package com.example.e_commerce.service;

import com.example.e_commerce.entity.*;
import com.example.e_commerce.repository.*;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.util.HexFormat;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Value("${app.razorpay.key-id}")
    private String keyId;

    @Value("${app.razorpay.key-secret}")
    private String keySecret;

    public Map<String, Object> createRazorpayOrder(Long orderId) throws RazorpayException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        RazorpayClient client = new RazorpayClient(keyId, keySecret);

        JSONObject options = new JSONObject();
        // Razorpay amount is in paise (1 INR = 100 paise)
        options.put("amount", order.getTotalAmount().multiply(BigDecimal.valueOf(100)).intValue());
        options.put("currency", "INR");
        options.put("receipt", "order_" + orderId);

        com.razorpay.Order razorpayOrder = client.orders.create(options);

        // Save payment record
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setRazorpayOrderId(razorpayOrder.get("id"));
        payment.setStatus(Payment.PaymentStatus.PENDING);
        paymentRepository.save(payment);

        return Map.of(
                "razorpayOrderId", razorpayOrder.get("id").toString(),
                "amount", razorpayOrder.get("amount").toString(),
                "currency", "INR",
                "keyId", keyId
        );
    }

    public Payment verifyAndSave(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        // Verify signature: HMAC-SHA256(razorpayOrderId + "|" + razorpayPaymentId, keySecret)
        if (!verifySignature(razorpayOrderId, razorpayPaymentId, razorpaySignature)) {
            throw new RuntimeException("Payment verification failed: invalid signature");
        }

        Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.getOrder().setStatus(Order.OrderStatus.CONFIRMED);
        orderRepository.save(payment.getOrder());
        return paymentRepository.save(payment);
    }

    private boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keySecret.getBytes(), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes());
            String generated = HexFormat.of().formatHex(hash);
            return generated.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }
}
