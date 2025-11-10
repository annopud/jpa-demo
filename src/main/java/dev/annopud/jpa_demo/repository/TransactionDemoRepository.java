package dev.annopud.jpa_demo.repository;

import dev.annopud.jpa_demo.entity.TransactionDemoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionDemoRepository extends JpaRepository<TransactionDemoEntity, Long> {
}
