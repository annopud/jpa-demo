package dev.annopud.jpa_demo.service;

import dev.annopud.jpa_demo.entity.TransactionDemoEntity;
import dev.annopud.jpa_demo.repository.TransactionDemoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inner service for demonstrating transactional propagation.
 * Separated from TxDemoService to ensure proper Spring AOP proxy interception.
 * 
 * IMPORTANT: Self-invocation of @Transactional methods in the same bean does NOT
 * apply transactional advice. This separate service ensures that when TxDemoService
 * calls these methods, they are properly intercepted by Spring's transaction proxy.
 */
@Service
public class TxDemoInnerService {
    
    private static final Logger log = LoggerFactory.getLogger(TxDemoInnerService.class);
    
    private final TransactionDemoRepository repository;
    
    public TxDemoInnerService(TransactionDemoRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Inner method with REQUIRED propagation (default).
     * Joins the existing transaction from the outer method.
     * If it throws an uncaught exception, the entire transaction (outer + inner) is rolled back.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void innerRequired_throwsException() {
        log.info(">>> INNER (REQUIRED): Saving entity before throwing exception");
        repository.save(new TransactionDemoEntity("innerRequired", "This should be rolled back"));
        log.info(">>> INNER (REQUIRED): About to throw RuntimeException");
        throw new RuntimeException("Inner REQUIRED method failed");
    }
    
    /**
     * Inner method with REQUIRED propagation that succeeds.
     * Joins the existing transaction from the outer method.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void innerRequired_succeeds() {
        log.info(">>> INNER (REQUIRED): Saving entity successfully");
        repository.save(new TransactionDemoEntity("innerRequired_success", "This should be committed"));
        log.info(">>> INNER (REQUIRED): Completed successfully");
    }
    
    /**
     * Inner method with NESTED propagation.
     * Creates a savepoint when called within an existing transaction.
     * If it throws an exception and the outer method catches it, only this inner
     * part is rolled back to the savepoint, and the outer transaction can continue.
     * 
     * NOTE: NESTED requires a transaction manager that supports savepoints
     * (DataSourceTransactionManager + JDBC DB with savepoint support like H2, PostgreSQL, MySQL).
     */
    @Transactional(propagation = Propagation.NESTED)
    public void innerNested_throwsException() {
        log.info(">>> INNER (NESTED): Saving entity before throwing exception");
        repository.save(new TransactionDemoEntity("innerNested", "This should be rolled back to savepoint"));
        log.info(">>> INNER (NESTED): About to throw RuntimeException");
        throw new RuntimeException("Inner NESTED method failed");
    }
    
    /**
     * Inner method with NESTED propagation that succeeds.
     */
    @Transactional(propagation = Propagation.NESTED)
    public void innerNested_succeeds() {
        log.info(">>> INNER (NESTED): Saving entity successfully");
        repository.save(new TransactionDemoEntity("innerNested_success", "This should be committed"));
        log.info(">>> INNER (NESTED): Completed successfully");
    }
    
    /**
     * Inner method that marks the transaction as rollback-only.
     * Even if no exception is thrown and the outer method catches any exceptions,
     * the entire transaction will be rolled back.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void innerRequired_marksRollbackOnly() {
        log.info(">>> INNER (REQUIRED): Saving entity");
        repository.save(new TransactionDemoEntity("innerWithRollbackOnly", "This should be rolled back"));
        log.info(">>> INNER (REQUIRED): Marking transaction as rollback-only");
        org.springframework.transaction.interceptor.TransactionAspectSupport
            .currentTransactionStatus()
            .setRollbackOnly();
        log.info(">>> INNER (REQUIRED): Transaction marked as rollback-only");
    }
}
