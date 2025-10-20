package dev.annopud.jpa_demo.repository;

import dev.annopud.jpa_demo.entity.GeneralTesting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GeneralTestingRepository extends JpaRepository<GeneralTesting, String>, JpaSpecificationExecutor<GeneralTesting> {

}