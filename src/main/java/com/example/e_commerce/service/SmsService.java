package com.example.e_commerce.service;

import com.example.e_commerce.entity.Order;
import com.example.e_commerce.repository.PaymentRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-number}")
    private String fromNumber;

    @Value("${app.admin.phone}")
    private String adminPhone;

    private final PaymentRepository paymentRepository;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    @Async
    public void sendOrderNotificationToAdmin(Order order) {
        String products = order.getItems().stream()
                .map(oi -> oi.getProduct().getName()
                        + (oi.getSelectedImage() != null ? " [" + oi.getSelectedImage() + "]" : "")
                        + " x" + oi.getQuantity())
                .collect(Collectors.joining(", "));

        String paymentStatus = paymentRepository.findByOrder(order)
                .map(p -> p.getStatus().name())
                .orElse("PENDING");

        String customerPhone = order.getUser().getPhone() != null ? order.getUser().getPhone() : "N/A";

        String body = "New Order #" + order.getId() + "\n" +
                "Customer: " + order.getUser().getName() + "\n" +
                "Phone: " + customerPhone + "\n" +
                "Address: " + order.getShippingAddress() + "\n" +
                "Products: " + products + "\n" +
                "Total: Rs." + order.getTotalAmount() + "\n" +
                "Payment: " + paymentStatus;

        Message.creator(new PhoneNumber(adminPhone), new PhoneNumber(fromNumber), body).create();
    }
}
