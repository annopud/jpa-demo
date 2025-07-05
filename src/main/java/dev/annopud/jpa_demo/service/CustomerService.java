package dev.annopud.jpa_demo.service;

import dev.annopud.jpa_demo.entity.Client;
import dev.annopud.jpa_demo.entity.Customer;
import dev.annopud.jpa_demo.repository.ClientRepository;
import dev.annopud.jpa_demo.repository.CustomerRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    static final Logger log = LoggerFactory.getLogger(CustomerService.class);
    private final CustomerRepository customerRepository;
    private final ClientRepository clientRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public CustomerService(CustomerRepository customerRepository, ClientRepository clientRepository) {
        this.customerRepository = customerRepository;
        this.clientRepository = clientRepository;
    }

    @Transactional
    public void experimentMethod() {
        Customer customer = new Customer("first", "last2");
        log.info("******* before save 1: {}******* ", customer);
        customerRepository.save(customer);
        log.info("******* after save 1: {}******* ", customer);
        log.info("1------------------------------------------------");

        log.info("------- before findByLastName: {} -------", customer.getLastName());
        List<Customer> findByLastName = customerRepository.findByLastName(customer.getLastName());
        Customer customer1 = findByLastName.stream()
            .findFirst()
            .orElseThrow();
        log.info("------- after findByLastName: {} -------", customer);
        log.info("2------------------------------------------------");

        log.info("******* before set firstName: {} *******", customer1);
        customer1.setFirstName("updatedFirst");
        log.info("******* after set firstName: {} *******", customer1);
        log.info("3------------------------------------------------");

        log.info("------- before save 2: {} -------", customer);
        customerRepository.save(customer1);
        log.info("------- after save 2: {} -------", customer);
        log.info("4------------------------------------------------");

        log.info("-*-*-*-* before findById: {} -*-*-*-*", customer1);
        Customer ctm = customerRepository.findById(1);
        log.info("-*-*-*-* after findById: {} -*-*-*-*", ctm);
        log.info("5------------------------------------------------");

        log.info("before set 3: {}", ctm);
        ctm.setFirstName("test");
        log.info("after set 3: {}", ctm);
        log.info("6------------------------------------------------");

        log.info("-----------before experiment section: {}", ctm);
//        Customer result = customerRepository.findByIdNative(ctm.getId().intValue());
//        Iterable<Customer> result = customerRepository.findAll();
//        List<Customer> result = customerRepository.findByLastName(ctm.getLastName());
//        Client result = clientRepository.findById(1);
        Client result = clientRepository.findByIdNative(1);
        log.info("end of task2");
        log.info("7------------------------------------------------");
    }

    public void regularMethod() {
        Customer customer = customerRepository.findById(1);
        log.info("before update: {}", customer);
        customer.setFirstName("updatedFirstxxxxxxxxxxxxxx");
    }

    @Transactional
    public void transactionalMethod() {
        log.info("Calling getCustomerById");
        Customer customer = customerRepository.findById(1);
        customer.setFirstName("transactionalMethod");
        log.info("Finished calling getCustomerById");
    }
}
