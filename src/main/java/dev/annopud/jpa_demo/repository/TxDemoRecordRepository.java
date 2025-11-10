package dev.annopud.jpa_demo.repository;

import dev.annopud.jpa_demo.entity.TxDemoRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TxDemoRecordRepository extends JpaRepository<TxDemoRecord, Long> {

}

