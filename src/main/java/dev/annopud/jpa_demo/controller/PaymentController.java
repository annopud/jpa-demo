package dev.annopud.jpa_demo.controller;

import dev.annopud.jpa_demo.model.CreatePaymentRequest;
import dev.annopud.jpa_demo.model.PaymentResponse;
import dev.annopud.jpa_demo.service.IdempotencyService;
import dev.annopud.jpa_demo.service.PaymentService;
import dev.annopud.jpa_demo.service.RetryableIdempotencyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final IdempotencyService idempotencyService;
    private final PaymentService paymentService;
    private final RetryableIdempotencyService retryableIdempotencyService;

    public PaymentController(IdempotencyService idempotencyService,
                             PaymentService paymentService,
                             RetryableIdempotencyService retryableIdempotencyService) {
        this.idempotencyService = idempotencyService;
        this.paymentService = paymentService;
        this.retryableIdempotencyService = retryableIdempotencyService;
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

    /**
     * Demo endpoint showing retry behavior:
     * - First call: executes business logic
     * - If successful, subsequent calls return 409 Conflict with warning
     * - If failed, allows up to 3 retry attempts
     * - After 3 failures, returns 429 Too Many Requests
     */
    @PostMapping("/retry-demo")
    public ResponseEntity<?> createPaymentWithRetryDemo(
        @RequestHeader("Idempotency-Key") String idempotencyKey,
        @RequestBody CreatePaymentRequest request,
        @RequestParam(defaultValue = "false") boolean simulateFailure
    ) {
        return retryableIdempotencyService.handleWithRetry(
            idempotencyKey,
            request,
            () -> {
                if (simulateFailure) {
                    throw new RuntimeException("Simulated payment failure");
                }
                PaymentResponse result = paymentService.createPayment(request);
                return ResponseEntity.status(201).body(result);
            }
        );
    }

    /**
     * Check the status and retry count of an idempotency key
     */
    @GetMapping("/status/{idempotencyKey}")
    public ResponseEntity<Map<String, Object>> checkStatus(
        @PathVariable String idempotencyKey
    ) {
        return retryableIdempotencyService.getStatus(idempotencyKey);
    }
}
