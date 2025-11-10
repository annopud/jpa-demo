package dev.annopud.jpa_demo.controller;

import dev.annopud.jpa_demo.service.TransactionDemoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tx-demo")
public class TransactionDemoController {

    private final TransactionDemoService service;

    public TransactionDemoController(TransactionDemoService service) {
        this.service = service;
    }

    @GetMapping("/clear")
    public ResponseEntity<String> clear() {
        service.clearAll();
        return ResponseEntity.ok("cleared");
    }

    @GetMapping("/list")
    public ResponseEntity<List<String>> list() {
        return ResponseEntity.ok(service.listAllTags());
    }

    @GetMapping("/required-required-noCatch")
    public ResponseEntity<Map<String, Object>> requiredRequiredNoCatch() {
        service.clearAll();
        Map<String, Object> result = new HashMap<>();
        try {
            service.requiredRequiredNoCatch();
            result.put("status", "completed");
        } catch (Exception e) {
            result.put("status", "exception: " + e.getMessage());
        }
        result.put("rows", service.listAllTags());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/required-required-catch")
    public ResponseEntity<Map<String, Object>> requiredRequiredCatch() {
        service.clearAll();
        Map<String, Object> result = new HashMap<>();
        service.requiredRequiredCatch();
        result.put("rows", service.listAllTags());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/required-requiresNew-catch")
    public ResponseEntity<Map<String, Object>> requiredRequiresNewCatch() {
        service.clearAll();
        Map<String, Object> result = new HashMap<>();
        service.requiredRequiresNewCatch();
        result.put("rows", service.listAllTags());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/required-requiresNew-noCatch")
    public ResponseEntity<Map<String, Object>> requiredRequiresNewNoCatch() {
        service.clearAll();
        Map<String, Object> result = new HashMap<>();
        try {
            service.requiredRequiresNewNoCatch();
            result.put("status", "completed");
        } catch (Exception e) {
            result.put("status", "exception: " + e.getMessage());
        }
        result.put("rows", service.listAllTags());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/required-nested-catch")
    public ResponseEntity<Map<String, Object>> requiredNestedCatch() {
        service.clearAll();
        Map<String, Object> result = new HashMap<>();
        service.requiredNestedCatch();
        result.put("rows", service.listAllTags());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/required-nested-noCatch")
    public ResponseEntity<Map<String, Object>> requiredNestedNoCatch() {
        service.clearAll();
        Map<String, Object> result = new HashMap<>();
        try {
            service.requiredNestedNoCatch();
            result.put("status", "completed");
        } catch (Exception e) {
            result.put("status", "exception: " + e.getMessage());
        }
        result.put("rows", service.listAllTags());
        return ResponseEntity.ok(result);
    }
}
