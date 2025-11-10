package dev.annopud.jpa_demo.controller;

import dev.annopud.jpa_demo.entity.TransactionDemoEntity;
import dev.annopud.jpa_demo.repository.TransactionDemoRepository;
import dev.annopud.jpa_demo.service.TxDemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller demonstrating @Transactional propagation behaviors.
 * 
 * To use this demo:
 * 1. Start the application with the 'txdemo' profile: --spring.profiles.active=txdemo
 * 2. Call each endpoint to see different transaction behaviors
 * 3. Use GET /tx-demo/records to see what was actually committed to the database
 * 4. Use DELETE /tx-demo/records to clear the database between tests
 * 
 * Access H2 console at: http://localhost:8080/h2-console
 * JDBC URL: jdbc:h2:mem:txdemo
 * Username: sa
 * Password: (empty)
 */
@RestController
@RequestMapping("/tx-demo")
public class TransactionDemoController {
    
    private static final Logger log = LoggerFactory.getLogger(TransactionDemoController.class);
    
    private final TxDemoService txDemoService;
    private final TransactionDemoRepository repository;
    
    public TransactionDemoController(TxDemoService txDemoService, TransactionDemoRepository repository) {
        this.txDemoService = txDemoService;
        this.repository = repository;
    }
    
    /**
     * Scenario 1: REQUIRED propagation - Inner exception is NOT caught
     * Expected result: NO records in database (entire transaction rolled back)
     */
    @PostMapping("/scenario1-required-uncaught")
    public ResponseEntity<Map<String, Object>> scenario1() {
        log.info("\n========================================");
        log.info("Starting Scenario 1: REQUIRED - Inner fails (uncaught)");
        log.info("========================================\n");
        
        try {
            txDemoService.scenario1_RequiredInnerFails_Uncaught();
            return createResponse("Scenario 1", "Completed (unexpected)", getRecordCount());
        } catch (Exception e) {
            log.info("\n>>> CONTROLLER: Exception propagated to controller: {}", e.getMessage());
            return createResponse("Scenario 1", "Exception caught in controller: " + e.getMessage(), getRecordCount());
        }
    }
    
    /**
     * Scenario 2: REQUIRED propagation - Inner exception IS caught
     * Expected result: NO records in database (transaction marked rollback-only)
     */
    @PostMapping("/scenario2-required-caught")
    public ResponseEntity<Map<String, Object>> scenario2() {
        log.info("\n========================================");
        log.info("Starting Scenario 2: REQUIRED - Inner fails (caught)");
        log.info("========================================\n");
        
        try {
            txDemoService.scenario2_RequiredInnerFails_Caught();
            return createResponse("Scenario 2", "Completed (but rolled back)", getRecordCount());
        } catch (Exception e) {
            log.info("\n>>> CONTROLLER: Exception propagated to controller: {}", e.getMessage());
            return createResponse("Scenario 2", "Exception: " + e.getMessage(), getRecordCount());
        }
    }
    
    /**
     * Scenario 3: NESTED propagation - Inner exception IS caught
     * Expected result: 2 records in database (outer saves committed, inner rolled back to savepoint)
     */
    @PostMapping("/scenario3-nested-caught")
    public ResponseEntity<Map<String, Object>> scenario3() {
        log.info("\n========================================");
        log.info("Starting Scenario 3: NESTED - Inner fails (caught)");
        log.info("========================================\n");
        
        try {
            txDemoService.scenario3_NestedInnerFails_Caught();
            return createResponse("Scenario 3", "Completed successfully", getRecordCount());
        } catch (Exception e) {
            log.info("\n>>> CONTROLLER: Exception propagated to controller: {}", e.getMessage());
            return createResponse("Scenario 3", "Exception: " + e.getMessage(), getRecordCount());
        }
    }
    
    /**
     * Scenario 4: NESTED propagation - Inner exception is NOT caught
     * Expected result: NO records in database (entire transaction rolled back)
     */
    @PostMapping("/scenario4-nested-uncaught")
    public ResponseEntity<Map<String, Object>> scenario4() {
        log.info("\n========================================");
        log.info("Starting Scenario 4: NESTED - Inner fails (uncaught)");
        log.info("========================================\n");
        
        try {
            txDemoService.scenario4_NestedInnerFails_Uncaught();
            return createResponse("Scenario 4", "Completed (unexpected)", getRecordCount());
        } catch (Exception e) {
            log.info("\n>>> CONTROLLER: Exception propagated to controller: {}", e.getMessage());
            return createResponse("Scenario 4", "Exception caught in controller: " + e.getMessage(), getRecordCount());
        }
    }
    
    /**
     * Scenario 5: REQUIRED propagation - Inner sets rollback-only flag
     * Expected result: NO records in database (transaction marked rollback-only)
     */
    @PostMapping("/scenario5-required-rollbackonly")
    public ResponseEntity<Map<String, Object>> scenario5() {
        log.info("\n========================================");
        log.info("Starting Scenario 5: REQUIRED - Inner sets rollback-only");
        log.info("========================================\n");
        
        try {
            txDemoService.scenario5_RequiredInner_SetsRollbackOnly();
            return createResponse("Scenario 5", "Completed (but rolled back)", getRecordCount());
        } catch (Exception e) {
            log.info("\n>>> CONTROLLER: Exception propagated to controller: {}", e.getMessage());
            return createResponse("Scenario 5", "Exception: " + e.getMessage(), getRecordCount());
        }
    }
    
    /**
     * Scenario 6: REQUIRED propagation - Both outer and inner succeed
     * Expected result: 2 records in database (all committed)
     */
    @PostMapping("/scenario6-required-success")
    public ResponseEntity<Map<String, Object>> scenario6() {
        log.info("\n========================================");
        log.info("Starting Scenario 6: REQUIRED - Both succeed");
        log.info("========================================\n");
        
        try {
            txDemoService.scenario6_RequiredBothSucceed();
            return createResponse("Scenario 6", "Completed successfully", getRecordCount());
        } catch (Exception e) {
            log.info("\n>>> CONTROLLER: Exception propagated to controller: {}", e.getMessage());
            return createResponse("Scenario 6", "Exception: " + e.getMessage(), getRecordCount());
        }
    }
    
    /**
     * Scenario 7: NESTED propagation - Both outer and inner succeed
     * Expected result: 2 records in database (all committed)
     */
    @PostMapping("/scenario7-nested-success")
    public ResponseEntity<Map<String, Object>> scenario7() {
        log.info("\n========================================");
        log.info("Starting Scenario 7: NESTED - Both succeed");
        log.info("========================================\n");
        
        try {
            txDemoService.scenario7_NestedBothSucceed();
            return createResponse("Scenario 7", "Completed successfully", getRecordCount());
        } catch (Exception e) {
            log.info("\n>>> CONTROLLER: Exception propagated to controller: {}", e.getMessage());
            return createResponse("Scenario 7", "Exception: " + e.getMessage(), getRecordCount());
        }
    }
    
    /**
     * Get all records currently in the database.
     * Use this to verify what was actually committed vs rolled back.
     */
    @GetMapping("/records")
    public ResponseEntity<Map<String, Object>> getAllRecords() {
        List<TransactionDemoEntity> records = repository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("count", records.size());
        response.put("records", records);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Clear all records from the database.
     * Use this to reset between tests.
     */
    @DeleteMapping("/records")
    public ResponseEntity<Map<String, Object>> clearAllRecords() {
        long count = repository.count();
        repository.deleteAll();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Deleted all records");
        response.put("deletedCount", count);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Welcome endpoint with usage instructions.
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Spring @Transactional Propagation Demo");
        response.put("instructions", "Use POST endpoints to run scenarios, GET /records to see results");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("POST /tx-demo/scenario1-required-uncaught", "REQUIRED: Inner fails, exception uncaught → Full rollback");
        endpoints.put("POST /tx-demo/scenario2-required-caught", "REQUIRED: Inner fails, exception caught → Rollback-only");
        endpoints.put("POST /tx-demo/scenario3-nested-caught", "NESTED: Inner fails, exception caught → Savepoint rollback, outer commits");
        endpoints.put("POST /tx-demo/scenario4-nested-uncaught", "NESTED: Inner fails, exception uncaught → Full rollback");
        endpoints.put("POST /tx-demo/scenario5-required-rollbackonly", "REQUIRED: Inner sets rollback-only → Full rollback");
        endpoints.put("POST /tx-demo/scenario6-required-success", "REQUIRED: Both succeed → All committed");
        endpoints.put("POST /tx-demo/scenario7-nested-success", "NESTED: Both succeed → All committed");
        endpoints.put("GET /tx-demo/records", "View all records in database");
        endpoints.put("DELETE /tx-demo/records", "Clear all records");
        
        response.put("endpoints", endpoints);
        response.put("currentRecordCount", getRecordCount());
        
        return ResponseEntity.ok(response);
    }
    
    private ResponseEntity<Map<String, Object>> createResponse(String scenario, String status, long recordCount) {
        Map<String, Object> response = new HashMap<>();
        response.put("scenario", scenario);
        response.put("status", status);
        response.put("recordCount", recordCount);
        response.put("message", "Check logs for detailed transaction flow. Use GET /tx-demo/records to see actual data.");
        return ResponseEntity.ok(response);
    }
    
    private long getRecordCount() {
        return repository.count();
    }
}
