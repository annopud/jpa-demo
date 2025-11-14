package dev.annopud.jpa_demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.annopud.jpa_demo.entity.IdempotencyRecord;
import dev.annopud.jpa_demo.entity.IdempotencyStatus;
import dev.annopud.jpa_demo.repository.IdempotencyRecordRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class IdempotencyService {

    private static final int MAX_RETRIES = 3;

    private final IdempotencyRecordRepository repository;
    private final ObjectMapper objectMapper;

    public IdempotencyService(IdempotencyRecordRepository repository,
                              ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public <T> ResponseEntity<T> handle(
        String idempotencyKey,
        Object requestBodyObject,
        Supplier<ResponseEntity<T>> businessLogic) {

        String requestJson = toJson(requestBodyObject);
        String requestHash = sha256(requestJson);

        Optional<IdempotencyRecord> existingOpt =
            repository.findByIdempotencyKey(idempotencyKey);

        if (existingOpt.isPresent()) {
            IdempotencyRecord record = existingOpt.get();

            // same key but different body
            if (!record.getRequestHash().equals(requestHash)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            // still processing: we don't increment retry count yet
            if (record.getStatus() == IdempotencyStatus.PROCESSING) {
                return ResponseEntity.status(HttpStatus.ACCEPTED).build();
            }

            // SUCCESS or FAILED: try to increment retryCount atomically
            int updatedRows = repository.incrementRetryCountIfBelowLimit(record.getId(), MAX_RETRIES);
            if (updatedRows == 0) {
                // someone else already used up the remaining retries
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
            }

            // we are within retry limit → replay stored response
            @SuppressWarnings("unchecked")
            T body = (T) fromJson(record.getResponseBody(), Object.class);

            return ResponseEntity
                .status(record.getResponseStatusCode())
                .body(body);
        }

        // === First time we see this key (same as before) ===
        IdempotencyRecord record = new IdempotencyRecord();
        record.setIdempotencyKey(idempotencyKey);
        record.setRequestHash(requestHash);
        record.setStatus(IdempotencyStatus.PROCESSING);
        record.setRetryCount(0);

        try {
            repository.saveAndFlush(record);
        } catch (DataIntegrityViolationException e) {
            // another thread inserted same key concurrently
            IdempotencyRecord concurrent = repository.findByIdempotencyKey(idempotencyKey)
                .orElseThrow();
            return reuseExistingRecord(concurrent);
        }

        ResponseEntity<T> response;
        try {
            response = businessLogic.get();
            record.setStatus(IdempotencyStatus.SUCCESS);
        } catch (Exception ex) {
            record.setStatus(IdempotencyStatus.FAILED);
            response = ResponseEntity.internalServerError().build();
        }

        record.setResponseStatusCode(response.getStatusCode().value());
        record.setResponseBody(toJson(response.getBody()));
        repository.save(record);

        return response;
    }

    private <T> ResponseEntity<T> reuseExistingRecord(IdempotencyRecord record) {
        // This is used in the race case after duplicate insert
        if (record.getStatus() == IdempotencyStatus.PROCESSING) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        }

        int updatedRows = repository.incrementRetryCountIfBelowLimit(record.getId(), MAX_RETRIES);
        if (updatedRows == 0) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        @SuppressWarnings("unchecked")
        T body = (T) fromJson(record.getResponseBody(), Object.class);

        return ResponseEntity
            .status(record.getResponseStatusCode())
            .body(body);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash request", e);
        }
    }

    private String toJson(Object obj) {
        if (obj == null) return "null";
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize body", e);
        }
    }

    private Object fromJson(String json, Class<?> type) {
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize body", e);
        }
    }
}
