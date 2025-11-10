package dev.annopud.jpa_demo.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Transaction configuration for the demo.
 * 
 * Important: NESTED propagation requires a transaction manager that supports savepoints.
 * 
 * JpaTransactionManager can support nested transactions by setting nestedTransactionAllowed to true.
 * When enabled, NESTED propagation will create JDBC savepoints for partial rollback within the same
 * JPA EntityManager context.
 * 
 * Note: This works with databases that support JDBC savepoints (H2, PostgreSQL, MySQL, etc.)
 * but has limitations:
 * - Entity state changes may not be properly isolated between nested levels
 * - The EntityManager cache is shared across savepoints
 * - For production use, consider using separate services with REQUIRES_NEW instead of NESTED
 * 
 * This configuration is activated only for the 'txdemo' profile to avoid affecting other parts of the application.
 */
@Configuration
@Profile("txdemo")
public class TransactionConfig {
    
    /**
     * Configure JpaTransactionManager with nested transaction support.
     * This allows NESTED transaction propagation to work with JPA entities.
     */
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager(entityManagerFactory);
        // Enable nested transaction support (JDBC savepoints)
        transactionManager.setNestedTransactionAllowed(true);
        return transactionManager;
    }
}
