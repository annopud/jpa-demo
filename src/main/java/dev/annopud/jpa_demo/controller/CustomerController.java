package dev.annopud.jpa_demo.controller;

import dev.annopud.jpa_demo.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("")
    public ResponseEntity<String> experimentMethod() {
        customerService.experimentMethod();
        return ResponseEntity.ok("Customer processed successfully");
    }

    @GetMapping("/regular")
    public ResponseEntity<String> regularMethod() {
        customerService.regularMethod();
        return ResponseEntity.ok("Regular method executed successfully");
    }

    @GetMapping("/transactional")
    public ResponseEntity<String> transactionalMethod() {
        customerService.transactionalMethod();
        return ResponseEntity.ok("Transactional method executed successfully");
    }
}
