package dev.annopud.jpa_demo.repository;

import dev.annopud.jpa_demo.entity.Client;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ClientRepository extends CrudRepository<Client, Long> {

    List<Client> findByLastName(String lastName);

    Client findById(long id);

    @Query(
        nativeQuery = true,
        value = """
         SELECT 
             c.id,
             c.first_name,
             c.last_name
         FROM client AS c
         WHERE c.id = :id"""
    )
    Client findByIdNative(long id);
}