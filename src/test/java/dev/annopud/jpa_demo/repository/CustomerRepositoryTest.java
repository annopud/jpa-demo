package dev.annopud.jpa_demo.repository;


import dev.annopud.jpa_demo.entity.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customers;

    @Test
    void testFindByLastName() {
        Customer customer = new Customer("first", "last");
        entityManager.persist(customer);

        List<Customer> findByLastName = customers.findByLastName(customer.getLastName());
        Customer customer1 = findByLastName.stream()
            .findFirst()
            .orElseThrow();
        customer1.setFirstName("updatedFirst");

        customers.save(customer1);


        List<Customer> test = customers.findByLastName("test");
//        Iterable<Customer> all = customers.findAll();
        assertThat(findByLastName).extracting(Customer::getLastName).containsOnly(customer.getLastName());
    }


    @Test
    void testConcurrentUpdates() throws InterruptedException {
        Runnable task1 = () -> {
            Customer customer = new Customer("first", "last");
            entityManager.persist(customer);

            List<Customer> findByLastName = customers.findByLastName(customer.getLastName());
            Customer customer1 = findByLastName.stream()
                .findFirst()
                .orElseThrow();
            customer1.setFirstName("updatedFirst");

            customers.save(customer1);


            List<Customer> test = customers.findByLastName("test");
        };

        Runnable task2 = () -> {
            Customer customer = new Customer("first", "last");
            entityManager.persist(customer);

            List<Customer> findByLastName = customers.findByLastName(customer.getLastName());
            Customer customer1 = findByLastName.stream()
                .findFirst()
                .orElseThrow();
            customer1.setFirstName("updatedFirst");

            customers.save(customer1);


            List<Customer> test = customers.findByLastName("test");
        };
        // Then
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(task1);
        executor.submit(task2);
        executor.shutdown();
        executor.awaitTermination(20, TimeUnit.SECONDS);

    }


}