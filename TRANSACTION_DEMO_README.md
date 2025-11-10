# Spring @Transactional Propagation Demo

This demo application illustrates how the `@org.springframework.transaction.annotation.Transactional` annotation works in different scenarios, particularly focusing on `Propagation.REQUIRED` and `Propagation.NESTED`, and demonstrating transaction rollback behaviors and scope.

## ⚠️ Important Note About NESTED Propagation

**NESTED propagation has limited support with JPA/Hibernate:**
- JpaTransactionManager with Hibernate's JpaDialect does NOT fully support JDBC savepoints
- While `setNestedTransactionAllowed(true)` can be set, Hibernate's JpaDialect may still reject savepoint operations
- Some NESTED scenarios (3, 7) may fail with "JpaDialect does not support savepoints" error
- REQUIRED propagation scenarios (1, 2, 5, 6) work perfectly and demonstrate the core concepts

**For production use:**
- Use `Propagation.REQUIRES_NEW` instead of NESTED for true transaction isolation
- Or use DataSourceTransactionManager with JDBC (not JPA) for full savepoint support
- The demo includes both working (REQUIRED) and educational (NESTED) scenarios

## Overview

The demo implements a REST API that exercises different transactional behaviors to show:
- How **REQUIRED** propagation works (joins existing transaction or creates new one)
- How **NESTED** propagation works (creates savepoints for partial rollback) - *with limitations noted above*
- When transactions are rolled back vs committed
- The effect of transaction scope and savepoint management
- Important Spring transaction caveats (proxy/self-invocation, visibility, etc.)

## Prerequisites

- Java 17 or higher
- Maven 3.6+

## Running the Demo

1. **Start the application** with the `txdemo` profile:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=txdemo
   ```

2. **Access the demo endpoints** at: `http://localhost:8080/tx-demo/`

3. **Access H2 Console** (optional) for direct database inspection:
   - URL: `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:mem:txdemo`
   - Username: `sa`
   - Password: (empty)

## API Endpoints

### Welcome & Instructions
- **GET** `/tx-demo/` - Welcome page with endpoint descriptions

### Database Operations
- **GET** `/tx-demo/records` - View all records currently in the database
- **DELETE** `/tx-demo/records` - Clear all records (useful for resetting between tests)

### Scenario Endpoints

#### Scenario 1: REQUIRED - Inner fails (uncaught)
```bash
curl -X POST http://localhost:8080/tx-demo/scenario1-required-uncaught
```
**Expected Behavior:**
- Outer saves 1 entity
- Inner (REQUIRED) joins the same transaction and saves 1 entity
- Inner throws RuntimeException (uncaught)
- **ENTIRE transaction is rolled back** (both outer and inner saves)
- **Result: 0 records in database**

#### Scenario 2: REQUIRED - Inner fails (caught)
```bash
curl -X POST http://localhost:8080/tx-demo/scenario2-required-caught
```
**Expected Behavior:**
- Outer saves 1 entity
- Inner (REQUIRED) joins the same transaction and saves 1 entity
- Inner throws RuntimeException
- Outer CATCHES the exception and continues
- Transaction is marked as rollback-only by the inner exception
- **ENTIRE transaction is rolled back** on commit attempt
- **Result: 0 records in database**

**Key Learning:** Catching an exception doesn't prevent rollback if the transaction is already marked rollback-only.

#### Scenario 3: NESTED - Inner fails (caught) ⭐
```bash
curl -X POST http://localhost:8080/tx-demo/scenario3-nested-caught
```
**Expected Behavior:**
- Outer saves 1 entity
- Inner (NESTED) creates a savepoint and saves 1 entity
- Inner throws RuntimeException
- Outer CATCHES the exception
- Transaction is rolled back to the savepoint (inner save is undone)
- Outer continues and saves another entity
- **Outer transaction commits successfully**
- **Result: 2 records in database** (outer saves only, no inner save)

**Key Learning:** NESTED allows partial rollback using savepoints. The outer transaction can continue after catching an inner exception.

#### Scenario 4: NESTED - Inner fails (uncaught)
```bash
curl -X POST http://localhost:8080/tx-demo/scenario4-nested-uncaught
```
**Expected Behavior:**
- Outer saves 1 entity
- Inner (NESTED) creates a savepoint and saves 1 entity
- Inner throws RuntimeException (uncaught)
- **ENTIRE transaction is rolled back**
- **Result: 0 records in database**

**Key Learning:** If the inner exception is not caught, even NESTED propagation will rollback everything.

#### Scenario 5: REQUIRED - Inner sets rollback-only
```bash
curl -X POST http://localhost:8080/tx-demo/scenario5-required-rollbackonly
```
**Expected Behavior:**
- Outer saves 1 entity
- Inner (REQUIRED) joins the same transaction and saves 1 entity
- Inner calls `setRollbackOnly()` but doesn't throw exception
- Outer continues and saves another entity
- **ENTIRE transaction is rolled back** on commit due to rollback-only flag
- **Result: 0 records in database**

**Key Learning:** `setRollbackOnly()` marks the entire transaction for rollback, even without an exception.

#### Scenario 6: REQUIRED - Both succeed
```bash
curl -X POST http://localhost:8080/tx-demo/scenario6-required-success
```
**Expected Behavior:**
- Outer saves 1 entity
- Inner (REQUIRED) joins the same transaction and saves 1 entity
- Both complete successfully
- **Transaction commits**
- **Result: 2 records in database**

#### Scenario 7: NESTED - Both succeed
```bash
curl -X POST http://localhost:8080/tx-demo/scenario7-nested-success
```
**Expected Behavior:**
- Outer saves 1 entity
- Inner (NESTED) creates a savepoint and saves 1 entity
- Both complete successfully
- **Transaction commits** (savepoint is released)
- **Result: 2 records in database**

## Example Test Flow

```bash
# 1. Start with the welcome page to see all endpoints
curl http://localhost:8080/tx-demo/

# 2. Test NESTED with caught exception (most interesting scenario)
curl -X POST http://localhost:8080/tx-demo/scenario3-nested-caught

# 3. Check what was actually committed
curl http://localhost:8080/tx-demo/records
# Expected: 2 records (outerBeforeInner and outerAfterInner)

# 4. Clear the database
curl -X DELETE http://localhost:8080/tx-demo/records

# 5. Test REQUIRED with caught exception (shows rollback-only behavior)
curl -X POST http://localhost:8080/tx-demo/scenario2-required-caught

# 6. Check what was committed
curl http://localhost:8080/tx-demo/records
# Expected: 0 records (entire transaction rolled back)
```

## Implementation Details

### Key Components

1. **TransactionDemoEntity** - Simple JPA entity for tracking transaction behavior
2. **TransactionDemoRepository** - JPA Repository for database operations
3. **TxDemoInnerService** - Separate service containing inner transactional methods
4. **TxDemoService** - Main service orchestrating transaction scenarios
5. **TransactionDemoController** - REST controller exposing demo endpoints

### Important Spring Transaction Concepts Demonstrated

#### 1. Self-Invocation Problem
**Problem:** Calling a `@Transactional` method from another method in the same bean does NOT apply transactional advice because Spring AOP uses proxies.

**Solution in this demo:** Inner transactional methods are in a separate service (`TxDemoInnerService`) to ensure proper proxy interception.

#### 2. REQUIRED Propagation (Default)
- If a transaction exists, join it
- If no transaction exists, create a new one
- If inner method throws an exception, the entire transaction is marked for rollback

#### 3. NESTED Propagation
- Requires a transaction manager that supports savepoints (DataSourceTransactionManager)
- If a transaction exists, creates a savepoint before executing
- If inner method fails and exception is caught, only rolls back to the savepoint
- If no outer transaction exists, behaves like REQUIRED
- **Important:** Not all transaction managers support NESTED (e.g., JTA typically doesn't)

#### 4. Rollback Rules
- By default, Spring rolls back on `RuntimeException` and `Error`
- Checked exceptions do NOT trigger rollback by default (can be configured with `rollbackFor`)
- `setRollbackOnly()` marks the entire transaction for rollback, not just nested parts

#### 5. Transaction Manager Requirements
- This demo uses H2 database with DataSourceTransactionManager
- H2, PostgreSQL, MySQL all support savepoints (required for NESTED)
- JTA transaction managers typically do NOT support nested transactions

## Observing Transaction Behavior

### In Application Logs
The application logs show detailed transaction flow:
- Transaction creation and propagation
- Entity save operations
- Exception handling
- Commit/rollback decisions

Look for log messages like:
- `>>> OUTER: ...` - Outer transaction operations
- `>>> INNER (REQUIRED): ...` - Inner operations with REQUIRED propagation
- `>>> INNER (NESTED): ...` - Inner operations with NESTED propagation
- `Creating new transaction` - New transaction started
- `Participating in existing transaction` - Joining existing transaction
- `Creating nested transaction` - Savepoint created

### In Database
Use the H2 console or the `/tx-demo/records` endpoint to verify actual committed data:
- Count records to see what survived rollback
- Check entity `name` and `context` fields to identify which scenario created them

## Summary of Results

| Scenario | Propagation | Exception Handling | Records Committed | Status |
|----------|-------------|-------------------|-------------------|---------|
| 1 | REQUIRED | Uncaught | 0 (full rollback) | ✅ Works |
| 2 | REQUIRED | Caught | 0 (rollback-only) | ✅ Works |
| 3 | NESTED | Caught | 2 (savepoint rollback) | ⚠️ May fail with JPA |
| 4 | NESTED | Uncaught | 0 (full rollback) | ⚠️ May fail with JPA |
| 5 | REQUIRED | setRollbackOnly | 0 (rollback-only) | ✅ Works |
| 6 | REQUIRED | No exception | 2 (all committed) | ✅ Works |
| 7 | NESTED | No exception | 2 (all committed) | ⚠️ May fail with JPA |

**Note:** NESTED scenarios (3, 4, 7) may fail with "JpaDialect does not support savepoints" when using JpaTransactionManager with Hibernate. This is a known limitation. The REQUIRED scenarios (1, 2, 5, 6) fully demonstrate transaction propagation concepts.

## Key Takeaways

1. **REQUIRED** joins existing transactions - failures affect the entire transaction ✅
2. **NESTED** uses savepoints - allows partial rollback when exceptions are caught (has JPA/Hibernate limitations) ⚠️
3. Catching an exception in REQUIRED doesn't prevent rollback if transaction is marked rollback-only ✅
4. `setRollbackOnly()` forces rollback regardless of exception handling ✅
5. Self-invocation bypasses Spring AOP - use separate beans for transactional methods ✅
6. NESTED requires savepoint support - JpaTransactionManager/Hibernate has limited support ⚠️
7. By default, only unchecked exceptions trigger rollback ✅
8. For production, prefer `REQUIRES_NEW` over `NESTED` for transaction isolation ✅

## Further Reading

- [Spring Transaction Management Documentation](https://docs.spring.io/spring-framework/reference/data-access/transaction.html)
- [Transaction Propagation](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-propagation.html)
- [Understanding @Transactional](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html)
