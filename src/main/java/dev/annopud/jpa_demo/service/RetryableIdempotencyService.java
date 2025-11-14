package dev.annopud.jpa_demo.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.annopud.jpa_demo.entity.IdempotencyRecord;
import dev.annopud.jpa_demo.entity.IdempotencyStatus;
import dev.annopud.jpa_demo.repository.IdempotencyRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
@Service
public class RetryableIdempotencyService {
    private static final Logger log = LoggerFactory.getLogger(RetryableIdempotencyService.class);
    private static final int MAX_RETRIES = 3;
    private final IdempotencyRecordRepository repository;
    private final ObjectMapper objectMapper;
    public RetryableIdempotencyService(IdempotencyRecordRepository repository,
                                       ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public <T> ResponseEntity<T> handleWithRetry(
        String idempotencyKey,
        Object requestBodyObject,
        Supplier<ResponseEntity<T>> businessLogic) {
        String requestJson = toJson(requestBodyObject);
        String requestHash = sha256(requestJson);
        Optional<IdempotencyRecord> existingOpt =
            repository.findByIdempotencyKeyWithLock(idempotencyKey);
        if (existingOpt.isPresent()) {
            return handleExistingRecord(existingOpt.get(), requestHash, businessLogic);
        }
        return handleNewRecord(idempotencyKey, requestHash, businessLogic);
    }
    private <T> ResponseEntity<T> handleExistingRecord(
        IdempotencyRecord rec,
        String requestHash,
        Supplier<ResponseEntity<T>> businessLogic) {
        if (!rec.getRequestHash().equals(requestHash)) {
            log.warn("Idempotency key '{}' reused with different request body", rec.getIdempotencyKey());
            return buildErrorResponse(
                HttpStatus.CONFLICT,
                "BODY_MISMATCH",
                "Request body differs from original request with this idempotency key"
            );
        }
        if (rec.getStatus() == IdempotencyStatus.PROCESSING) {
            log.info("Request '{}' still processing", rec.getIdempotencyKey());
            return buildErrorResponse(
                HttpStatus.ACCEPTED,
                "PROCESSING",
                "Request is currently being processed. Please wait."
            );
        }
        if (rec.getStatus() == IdempotencyStatus.SUCCESS) {
            log.warn("Operation '{}' already succeeded. Attempt to call again.", rec.getIdempotencyKey());
            return buildErrorResponse(
                HttpStatus.CONFLICT,
                "ALREADY_SUCCEEDED",
                String.format("Operation already completed successfully. Original response has been cached. Retry count: %d", rec.getRetryCount()),
                Map.of(
                    "originalStatusCode", rec.getResponseStatusCode(),
                    "completedAt", rec.getUpdatedAt().toString(),
                    "retryCount", rec.getRetryCount()
                )
            );
        }
        if (rec.getStatus() == IdempotencyStatus.FAILED) {
            if (rec.getRetryCount() >= MAX_RETRIES) {
                log.error("Max retries ({}) exceeded for key: {}", MAX_RETRIES, rec.getIdempotencyKey());
                return buildErrorResponse(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "MAX_RETRIES_EXCEEDED",
                    String.format("Maximum retry attempts (%d) exceeded. Operation has failed %d times.", MAX_RETRIES, rec.getRetryCount()),
                    Map.of(
                        "retryCount", rec.getRetryCount(),
                        "maxRetries", MAX_RETRIES,
                        "lastFailedAt", rec.getUpdatedAt().toString()
                    )
                );
            }
            int attemptNumber = rec.getRetryCount() + 1;
            rec.setRetryCount(attemptNumber);
            rec.setStatus(IdempotencyStatus.PROCESSING);
            repository.saveAndFlush(rec);
            log.info("Retrying failed operation '{}'. Attempt: {}/{}", rec.getIdempotencyKey(), attemptNumber, MAX_RETRIES);
            return executeBusinessLogic(rec, businessLogic, attemptNumber);
        }
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "UNKNOWN_STATE", "Unexpected record state");
    }
    private <T> ResponseEntity<T> handleNewRecord(
        String idempotencyKey,
        String requestHash,
        Supplier<ResponseEntity<T>> businessLogic) {
        IdempotencyRecord rec = new IdempotencyRecord();
        rec.setIdempotencyKey(idempotencyKey);
        rec.setRequestHash(requestHash);
        rec.setStatus(IdempotencyStatus.PROCESSING);
        rec.setRetryCount(0);
        try {
            repository.saveAndFlush(rec);
            log.info("Created new idempotency record for key: {}", idempotencyKey);
        } catch (DataIntegrityViolationException e) {
            log.info("Concurrent creation detected for key '{}', fetching existing record", idempotencyKey);
            IdempotencyRecord concurrent = repository.findByIdempotencyKeyWithLock(idempotencyKey)
                .orElseThrow(() -> new RuntimeException("Record disappeared after concurrent insert"));
            return handleExistingRecord(concurrent, requestHash, businessLogic);
        }
        return executeBusinessLogic(rec, businessLogic, 0);
    }
    private <T> ResponseEntity<T> executeBusinessLogic(
        IdempotencyRecord rec,
        Supplier<ResponseEntity<T>> businessLogic,
        int attemptNumber) {
        ResponseEntity<T> response;
        try {
            response = businessLogic.get();
            rec.setStatus(IdempotencyStatus.SUCCESS);
            log.info("Business logic succeeded for '{}' on attempt {} with status: {}", 
                rec.getIdempotencyKey(), attemptNumber, response.getStatusCode());
        } catch (Exception ex) {
            log.error("Business logic failed for '{}' on attempt {}: {}", 
                rec.getIdempotencyKey(), attemptNumber, ex.getMessage());
            rec.setStatus(IdempotencyStatus.FAILED);
            String errorMessage = attemptNumber >= MAX_RETRIES 
                ? String.format("Operation failed after %d attempts. Maximum retries reached.", MAX_RETRIES)
                : String.format("Operation failed on attempt %d of %d. You can retry.", attemptNumber, MAX_RETRIES);
            response = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "OPERATION_FAILED",
                errorMessage,
                Map.of(
                    "attemptNumber", attemptNumber,
                    "maxRetries", MAX_RETRIES,
                    "canRetry", attemptNumber < MAX_RETRIES,
                    "error", ex.getMessage()
                )
            );
        }
        rec.setResponseStatusCode(response.getStatusCode().value());
        rec.setResponseBody(toJson(response.getBody()));
        repository.save(rec);
        return response;
    }
    public ResponseEntity<Map<String, Object>> getStatus(String idempotencyKey) {
        Optional<IdempotencyRecord> recordOpt = repository.findByIdempotencyKey(idempotencyKey);
        if (recordOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "exists", false,
                "message", "No record found for this idempotency key"
            ));
        }
        IdempotencyRecord rec = recordOpt.get();
        return ResponseEntity.ok(Map.of(
            "exists", true,
            "idempotencyKey", rec.getIdempotencyKey(),
            "status", rec.getStatus().name(),
            "retryCount", rec.getRetryCount(),
            "maxRetries", MAX_RETRIES,
            "canRetry", rec.getStatus() == IdempotencyStatus.FAILED && rec.getRetryCount() < MAX_RETRIES,
            "responseStatusCode", rec.getResponseStatusCode() != null ? rec.getResponseStatusCode() : "N/A",
            "createdAt", rec.getCreatedAt().toString(),
            "updatedAt", rec.getUpdatedAt().toString()
        ));
    }
    private <T> ResponseEntity<T> buildErrorResponse(HttpStatus status, String errorCode, String message) {
        return buildErrorResponse(status, errorCode, message, null);
    }
    @SuppressWarnings("unchecked")
    private <T> ResponseEntity<T> buildErrorResponse(HttpStatus status, String errorCode, String message, Map<String, Object> details) {
        Map<String, Object> errorBody = new java.util.HashMap<>();
        errorBody.put("error", errorCode);
        errorBody.put("message", message);
        errorBody.put("status", status.value());
        if (details != null) {
            errorBody.putAll(details);
        }
        return (ResponseEntity<T>) ResponseEntity
            .status(status)
            .header("X-Error-Code", errorCode)
            .body(errorBody);
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
}
