package dev.annopud.jpa_demo.repository;

import dev.annopud.jpa_demo.entity.Customer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CustomerRepository extends CrudRepository<Customer, Long> {

    List<Customer> findByLastName(String lastName);

    Customer findById(long id);


    @Query(
        nativeQuery = true,
        value = """
         SELECT 
             c.id,
             c.first_name,
             c.last_name
         FROM mydatabase.customer AS c
         WHERE c.id = :id"""
    )
    Customer findByIdNative(long id);
}