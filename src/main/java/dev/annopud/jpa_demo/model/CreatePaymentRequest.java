package dev.annopud.jpa_demo.model;

import java.math.BigDecimal;

public record CreatePaymentRequest(
    BigDecimal amount,
    String currency,
    String reference
) {
}
