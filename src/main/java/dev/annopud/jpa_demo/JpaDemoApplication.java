package dev.annopud.jpa_demo;

import dev.annopud.jpa_demo.entity.Client;
import dev.annopud.jpa_demo.entity.Customer;
import dev.annopud.jpa_demo.repository.ClientRepository;
import dev.annopud.jpa_demo.repository.CustomerRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class JpaDemoApplication {

    private static final Logger log = LoggerFactory.getLogger(JpaDemoApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(JpaDemoApplication.class, args);
    }


    @Bean
    public CommandLineRunner demo(
        CustomerRepository customers,
        EntityManager entityManager,
        ClientRepository clientRepository
    ) {
        FlushModeType flushMode = entityManager.getFlushMode();
        log.info("Current Flush Mode: {}", flushMode);
        return (args) -> {
            Runnable task1 = () -> {
                Customer customer = new Customer("first", "last");
                customers.save(customer);

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
                log.info("Before save 1: {}", customer);
                customers.save(customer);

                log.info("before findByLastName 1: {}", customer.getLastName());
                List<Customer> findByLastName = customers.findByLastName(customer.getLastName());
                Customer customer1 = findByLastName.stream()
                    .findFirst()
                    .orElseThrow();
                customer1.setFirstName("updatedFirst");

                log.info("Before save 2: {}", customer);
                customers.save(customer1);
                log.info("after save: {}", customer1);


                log.info("Before findByLastName2: {}", findByLastName);
                List<Customer> test = customers.findByLastName("last");
//				Customer customer2 = test.stream()
//					.findFirst()
//					.orElseThrow();

                for (Customer ctm : test) {
                    ctm.setLastName("test");

                }


//				customer2.setFirstName("updatedSecond");

                log.info("before findAll: {}", test);
                Iterable<Customer> all = customers.findAll();
                System.out.println("end of task2");
            };
            // Then
//			ExecutorService executor = Executors.newFixedThreadPool(2);
////			executor.submit(task1);
//			executor.submit(task2);
//			executor.awaitTermination(10, TimeUnit.SECONDS);
////			executor.shutdown();
//			System.out.println("Finished executing tasks");

            Client client = new Client("first", "last");
            log.info("Before save 1: {}", client);
            clientRepository.save(client);
        };
    }
}
