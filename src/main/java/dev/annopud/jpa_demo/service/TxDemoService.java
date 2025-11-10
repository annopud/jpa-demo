package dev.annopud.jpa_demo.service;

import dev.annopud.jpa_demo.entity.TransactionDemoEntity;
import dev.annopud.jpa_demo.repository.TransactionDemoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service demonstrating various @Transactional propagation behaviors and rollback scenarios.
 * 
 * Key concepts demonstrated:
 * 1. REQUIRED propagation: Join existing transaction or create new one
 * 2. NESTED propagation: Create savepoint within existing transaction
 * 3. Rollback behaviors: Uncaught exceptions, caught exceptions, setRollbackOnly
 * 4. Transaction scope: What gets committed vs rolled back
 */
@Service
public class TxDemoService {
    
    private static final Logger log = LoggerFactory.getLogger(TxDemoService.class);
    
    private final TransactionDemoRepository repository;
    private final TxDemoInnerService innerService;
    
    public TxDemoService(TransactionDemoRepository repository, TxDemoInnerService innerService) {
        this.repository = repository;
        this.innerService = innerService;
    }
    
    /**
     * Scenario 1: REQUIRED propagation - Inner exception is NOT caught
     * 
     * Expected behavior:
     * - Outer saves one entity
     * - Inner (REQUIRED) joins the same transaction and saves one entity
     * - Inner throws RuntimeException
     * - Exception propagates to caller (not caught)
     * - ENTIRE transaction is rolled back (both outer and inner saves)
     * 
     * Result: NO entities are committed to the database
     */
    @Transactional
    public void scenario1_RequiredInnerFails_Uncaught() {
        log.info("=== SCENARIO 1: REQUIRED - Inner fails (uncaught) ===");
        log.info(">>> OUTER: Starting transaction, saving first entity");
        repository.save(new TransactionDemoEntity("outerBeforeInner", "scenario1"));
        
        log.info(">>> OUTER: Calling inner method (REQUIRED propagation)");
        // Inner will throw exception - NOT catching it
        innerService.innerRequired_throwsException();
        
        // This line is never reached
        log.info(">>> OUTER: After inner call (never reached)");
    }
    
    /**
     * Scenario 2: REQUIRED propagation - Inner exception IS caught
     * 
     * Expected behavior:
     * - Outer saves one entity
     * - Inner (REQUIRED) joins the same transaction and saves one entity
     * - Inner throws RuntimeException
     * - Outer CATCHES the exception and continues
     * - Despite catching the exception, the transaction is marked for rollback
     *   (by default, RuntimeException marks the entire transaction for rollback)
     * - Outer tries to save another entity
     * - On commit, transaction is rolled back due to rollback-only flag
     * 
     * Result: NO entities are committed (transaction marked rollback-only)
     */
    @Transactional
    public void scenario2_RequiredInnerFails_Caught() {
        log.info("=== SCENARIO 2: REQUIRED - Inner fails (caught) ===");
        log.info(">>> OUTER: Starting transaction, saving first entity");
        repository.save(new TransactionDemoEntity("outerBeforeInner", "scenario2"));
        
        try {
            log.info(">>> OUTER: Calling inner method (REQUIRED propagation)");
            innerService.innerRequired_throwsException();
        } catch (Exception e) {
            log.info(">>> OUTER: Caught exception from inner: {}", e.getMessage());
            log.info(">>> OUTER: Transaction is now marked as rollback-only");
        }
        
        log.info(">>> OUTER: Continuing after catching exception, saving another entity");
        repository.save(new TransactionDemoEntity("outerAfterInner", "scenario2"));
        log.info(">>> OUTER: Transaction will be rolled back on commit due to rollback-only flag");
    }
    
    /**
     * Scenario 3: NESTED propagation - Inner exception IS caught
     * 
     * Expected behavior:
     * - Outer saves one entity
     * - Inner (NESTED) creates a savepoint and saves one entity
     * - Inner throws RuntimeException
     * - Outer CATCHES the exception
     * - Transaction is rolled back to the savepoint (inner save is undone)
     * - Outer transaction continues and saves another entity
     * - Outer transaction commits successfully
     * 
     * Result: 2 entities are committed (outerBeforeInner and outerAfterInner)
     *         The innerNested entity is NOT committed (rolled back to savepoint)
     */
    @Transactional
    public void scenario3_NestedInnerFails_Caught() {
        log.info("=== SCENARIO 3: NESTED - Inner fails (caught) ===");
        log.info(">>> OUTER: Starting transaction, saving first entity");
        repository.save(new TransactionDemoEntity("outerBeforeInner", "scenario3"));
        
        try {
            log.info(">>> OUTER: Calling inner method (NESTED propagation)");
            innerService.innerNested_throwsException();
        } catch (Exception e) {
            log.info(">>> OUTER: Caught exception from inner: {}", e.getMessage());
            log.info(">>> OUTER: Transaction rolled back to savepoint, outer can continue");
        }
        
        log.info(">>> OUTER: Continuing after catching exception, saving another entity");
        repository.save(new TransactionDemoEntity("outerAfterInner", "scenario3"));
        log.info(">>> OUTER: Completing successfully, outer transaction will commit");
    }
    
    /**
     * Scenario 4: NESTED propagation - Inner exception is NOT caught
     * 
     * Expected behavior:
     * - Outer saves one entity
     * - Inner (NESTED) creates a savepoint and saves one entity
     * - Inner throws RuntimeException
     * - Exception propagates to caller (not caught)
     * - ENTIRE transaction is rolled back (both outer and inner)
     * 
     * Result: NO entities are committed (entire transaction rolled back)
     */
    @Transactional
    public void scenario4_NestedInnerFails_Uncaught() {
        log.info("=== SCENARIO 4: NESTED - Inner fails (uncaught) ===");
        log.info(">>> OUTER: Starting transaction, saving first entity");
        repository.save(new TransactionDemoEntity("outerBeforeInner", "scenario4"));
        
        log.info(">>> OUTER: Calling inner method (NESTED propagation)");
        // Inner will throw exception - NOT catching it
        innerService.innerNested_throwsException();
        
        // This line is never reached
        log.info(">>> OUTER: After inner call (never reached)");
    }
    
    /**
     * Scenario 5: REQUIRED propagation - Inner sets rollback-only flag
     * 
     * Expected behavior:
     * - Outer saves one entity
     * - Inner (REQUIRED) joins the same transaction and saves one entity
     * - Inner calls setRollbackOnly() on the transaction
     * - Inner completes without throwing exception
     * - Outer continues and saves another entity
     * - On commit, transaction is rolled back due to rollback-only flag
     * 
     * Result: NO entities are committed (transaction marked rollback-only)
     */
    @Transactional
    public void scenario5_RequiredInner_SetsRollbackOnly() {
        log.info("=== SCENARIO 5: REQUIRED - Inner sets rollback-only ===");
        log.info(">>> OUTER: Starting transaction, saving first entity");
        repository.save(new TransactionDemoEntity("outerBeforeInner", "scenario5"));
        
        log.info(">>> OUTER: Calling inner method (REQUIRED propagation)");
        innerService.innerRequired_marksRollbackOnly();
        
        log.info(">>> OUTER: Inner completed without exception, saving another entity");
        repository.save(new TransactionDemoEntity("outerAfterInner", "scenario5"));
        log.info(">>> OUTER: Transaction will be rolled back on commit due to rollback-only flag");
    }
    
    /**
     * Scenario 6: REQUIRED propagation - Both outer and inner succeed
     * 
     * Expected behavior:
     * - Outer saves one entity
     * - Inner (REQUIRED) joins the same transaction and saves one entity
     * - Both complete successfully
     * - Transaction commits
     * 
     * Result: 2 entities are committed (outerBeforeInner and innerRequired_success)
     */
    @Transactional
    public void scenario6_RequiredBothSucceed() {
        log.info("=== SCENARIO 6: REQUIRED - Both succeed ===");
        log.info(">>> OUTER: Starting transaction, saving first entity");
        repository.save(new TransactionDemoEntity("outerBeforeInner", "scenario6"));
        
        log.info(">>> OUTER: Calling inner method (REQUIRED propagation)");
        innerService.innerRequired_succeeds();
        
        log.info(">>> OUTER: Completing successfully, transaction will commit");
    }
    
    /**
     * Scenario 7: NESTED propagation - Both outer and inner succeed
     * 
     * Expected behavior:
     * - Outer saves one entity
     * - Inner (NESTED) creates a savepoint and saves one entity
     * - Both complete successfully
     * - Transaction commits (savepoint is released)
     * 
     * Result: 2 entities are committed (outerBeforeInner and innerNested_success)
     */
    @Transactional
    public void scenario7_NestedBothSucceed() {
        log.info("=== SCENARIO 7: NESTED - Both succeed ===");
        log.info(">>> OUTER: Starting transaction, saving first entity");
        repository.save(new TransactionDemoEntity("outerBeforeInner", "scenario7"));
        
        log.info(">>> OUTER: Calling inner method (NESTED propagation)");
        innerService.innerNested_succeeds();
        
        log.info(">>> OUTER: Completing successfully, transaction will commit");
    }
}
