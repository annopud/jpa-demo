package dev.annopud.jpa_demo.controller;

import dev.annopud.jpa_demo.model.CreatePaymentRequest;
import dev.annopud.jpa_demo.model.PaymentResponse;
import dev.annopud.jpa_demo.service.IdempotencyService;
import dev.annopud.jpa_demo.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final IdempotencyService idempotencyService;
    private final PaymentService paymentService;

    public PaymentController(IdempotencyService idempotencyService,
                             PaymentService paymentService) {
        this.idempotencyService = idempotencyService;
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
        @RequestHeader("Idempotency-Key") String idempotencyKey,
        @RequestBody CreatePaymentRequest request
    ) {
        return idempotencyService.handle(
            idempotencyKey,
            request,
            () -> {
                PaymentResponse result = paymentService.createPayment(request);
                // first call will return 201, retries will replay same response
                return ResponseEntity.status(201).body(result);
            }
        );
    }
}
