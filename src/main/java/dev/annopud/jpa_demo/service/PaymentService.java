package dev.annopud.jpa_demo.service;

import dev.annopud.jpa_demo.model.CreatePaymentRequest;
import dev.annopud.jpa_demo.model.PaymentResponse;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    public PaymentResponse createPayment(CreatePaymentRequest request) {
        // fake processing, e.g. insert into DB, call gateway, etc.
        String paymentId = UUID.randomUUID().toString();

        return new PaymentResponse(
            paymentId,
            request.amount(),
            request.currency(),
            "SUCCESS",
            OffsetDateTime.now()
        );
    }
}
