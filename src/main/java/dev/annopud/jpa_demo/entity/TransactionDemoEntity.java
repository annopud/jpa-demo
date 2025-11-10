package dev.annopud.jpa_demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "transaction_demo")
public class TransactionDemoEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column
    private String context;
    
    protected TransactionDemoEntity() {
    }
    
    public TransactionDemoEntity(String name, String context) {
        this.name = name;
        this.context = context;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getContext() {
        return context;
    }
    
    public void setContext(String context) {
        this.context = context;
    }
    
    @Override
    public String toString() {
        return "TransactionDemoEntity[id=%d, name='%s', context='%s']".formatted(
            id, name, context);
    }
}
