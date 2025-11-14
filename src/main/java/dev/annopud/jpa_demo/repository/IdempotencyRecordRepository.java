package dev.annopud.jpa_demo.repository;

import dev.annopud.jpa_demo.entity.IdempotencyRecord;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, Long> {

    Optional<IdempotencyRecord> findByIdempotencyKey(String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM IdempotencyRecord r WHERE r.idempotencyKey = :idempotencyKey")
    Optional<IdempotencyRecord> findByIdempotencyKeyWithLock(String idempotencyKey);

    @Modifying
    @Query("""
           update IdempotencyRecord r
           set r.retryCount = r.retryCount + 1
           where r.id = :id and r.retryCount < :maxRetries
           """)
    int incrementRetryCountIfBelowLimit(Long id, int maxRetries);
}
