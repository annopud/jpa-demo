package dev.annopud.jpa_demo.service;

import dev.annopud.jpa_demo.entity.TxDemoRecord;
import dev.annopud.jpa_demo.repository.TxDemoRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class TransactionDemoService {

    private static final Logger log = LoggerFactory.getLogger(TransactionDemoService.class);

    private final TxDemoRecordRepository repository;
    private final TransactionDemoService self; // proxy to call transactional methods

    public TransactionDemoService(TxDemoRecordRepository repository, @Lazy TransactionDemoService self) {
        this.repository = repository;
        this.self = self;
    }

    // Helper to create a record with an identifiable tag
    private void createRecord(String tag) {
        TxDemoRecord r = new TxDemoRecord();
        r.setTag(tag);
        r.setCreateDate(new Date());
        repository.save(r);
    }

    public List<String> listAllTags() {
        return repository.findAll().stream()
                .map(TxDemoRecord::getTag)
                .toList();
    }

    // ========== SCENARIOS ===========

    // 1) REQUIRED -> inner REQUIRED, inner throws, outer does NOT catch
    @Transactional(propagation = Propagation.REQUIRED)
    public void requiredRequiredNoCatch() {
        log.info("Start requiredRequiredNoCatch - will save outer then call inner that throws");
        createRecord("outer-required-required");
        self.innerRequiredThatThrows("inner-required-required");
    }

    // 2) REQUIRED -> inner REQUIRED, inner throws, outer catches
    @Transactional(propagation = Propagation.REQUIRED)
    public void requiredRequiredCatch() {
        log.info("Start requiredRequiredCatch - will save outer then call inner that throws but outer catches");
        createRecord("outer-required-required-catch");
        try {
            self.innerRequiredThatThrows("inner-required-required-catch");
        } catch (RuntimeException ex) {
            log.info("Caught inner exception inside outer (REQUIRED). Exception: {}", ex.getMessage());
            // swallow
        }
        // Attempt to save more after catching
        createRecord("outer-after-catch-required-required-catch");
    }

    // 3) REQUIRED -> inner REQUIRES_NEW, inner throws, outer CATCHS the exception
    @Transactional(propagation = Propagation.REQUIRED)
    public void requiredRequiresNewCatch() {
        log.info("Start requiredRequiresNewCatch - outer saves, inner (REQUIRES_NEW) throws but outer catches");
        createRecord("outer-required-requiresnew-catch");
        try {
            self.innerRequiresNewThatThrows("inner-requiresnew-that-throws");
        } catch (RuntimeException ex) {
            log.info("Caught inner exception from REQUIRES_NEW: {}", ex.getMessage());
        }
        // Continue and commit outer
        createRecord("outer-after-catch-required-requiresnew-catch");
    }

    // 4) REQUIRED -> inner REQUIRES_NEW, inner throws, outer does NOT catch
    @Transactional(propagation = Propagation.REQUIRED)
    public void requiredRequiresNewNoCatch() {
        log.info("Start requiredRequiresNewNoCatch - outer saves then calls inner (REQUIRES_NEW) that throws (no catch)");
        createRecord("outer-required-requiresnew-noCatch");
        self.innerRequiresNewThatThrows("inner-requiresnew-noCatch");
    }

    // 5) REQUIRED -> inner NESTED, inner throws, outer catches
    @Transactional(propagation = Propagation.REQUIRED)
    public void requiredNestedCatch() {
        log.info("Start requiredNestedCatch - outer saves, inner (NESTED) throws but outer catches");
        createRecord("outer-required-nested-catch");
        try {
            self.innerNestedThatThrows("inner-nested-that-throws");
        } catch (RuntimeException ex) {
            log.info("Caught inner exception from NESTED: {}", ex.getMessage());
        }
        // Continue and commit outer
        createRecord("outer-after-catch-required-nested-catch");
    }

    // 6) REQUIRED -> inner NESTED, inner throws, outer does NOT catch
    @Transactional(propagation = Propagation.REQUIRED)
    public void requiredNestedNoCatch() {
        log.info("Start requiredNestedNoCatch - outer saves then calls inner (NESTED) that throws (no catch)");
        createRecord("outer-required-nested-noCatch");
        self.innerNestedThatThrows("inner-nested-noCatch");
    }

    // ========== INNER IMPLEMENTATIONS ============

    @Transactional(propagation = Propagation.REQUIRED)
    public void innerRequiredThatThrows(String tag) {
        log.info("innerRequiredThatThrows: {}", tag);
        createRecord(tag);
        throw new RuntimeException("innerRequired failed: " + tag);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void innerRequiresNewThatThrows(String tag) {
        log.info("innerRequiresNewThatThrows: {}", tag);
        createRecord(tag);
        throw new RuntimeException("innerRequiresNew failed: " + tag);
    }

    @Transactional(propagation = Propagation.NESTED)
    public void innerNestedThatThrows(String tag) {
        log.info("innerNestedThatThrows: {}", tag);
        createRecord(tag);
        throw new RuntimeException("innerNested failed: " + tag);
    }

    // Utility to clear the table for clean demos
    public void clearAll() {
        repository.deleteAll();
        repository.flush();
    }
}
