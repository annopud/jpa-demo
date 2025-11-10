package dev.annopud.jpa_demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
public class Customer {
    private static final Logger log = LoggerFactory.getLogger(Customer.class);

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column
    private String firstName;
    @Column
    private String lastName;

    protected Customer() {
    }

    public Customer(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @PrePersist
    public void prePersist() {
        log.info("\n>>> PrePersist: {}", this);
    }

    @PostPersist
    public void postPersist() {
        log.info(">>> PostPersist: {}", this);
    }

    @PreUpdate
    public void preUpdate() {
        log.info(">>> PreUpdate: {}", this);
    }

    @PostUpdate
    public void postUpdate() {
        log.info(">>> PostUpdate: {}", this);
    }

    @PostLoad
    public void postLoad() {
        log.info(">>> PostLoad: {}", this);
    }

    @PreRemove
    public void preRemove() {
        log.info(">>> PreRemove: {}", this);
    }

    @PostRemove
    public void postRemove() {
        log.info(">>> PostRemove: {}", this);
    }

    @Override
    public String toString() {
        return "Customer[id=%d, firstName='%s', lastName='%s']".formatted(
            id, firstName, lastName);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}