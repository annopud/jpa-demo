package dev.annopud.jpa_demo.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PaymentResponse(
    String paymentId,
    BigDecimal amount,
    String currency,
    String status,
    OffsetDateTime createdAt
) {
}
