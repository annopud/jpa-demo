# JPA FlushModeType.AUTO Demonstration

This project demonstrates the behavior of flushing data to persistent storage in Java Persistence API (JPA) when using the default flush mode (AUTO).

## What is FlushModeType.AUTO?
With `FlushModeType.AUTO` (the default), JPA automatically flushes pending changes to the database before executing a JPQL query within a transaction. This means that newly persisted entities are visible to queries even before the transaction commits.

## How to Run

1. **Build and start the application:**
   - Using Maven:
     ```sh
     ./mvnw spring-boot:run
     ```
   - Or use your preferred IDE to run `JpaDemoApplication`.

2. **Trigger the demonstration endpoint:**
   - Send a GET request to:
     ```
     http://localhost:8080/customers
     ```
   - This will execute logic that:
     - Persists a new `Customer` entity.
     - Immediately queries for that customer by last name.
     - Shows (in logs) that the new customer is found by the query before the transaction commits.

## What to Observe

Check the application logs after calling the endpoint. You will see log statements like:

```
Before save 1: Customer(...)
after save 1: Customer(...)
before findByLastName 1: ...
after findByLastName 1: ...
```

Notice when the sql statements of `INSERT` and `UPDATE` operations are executed,
to proof that the `Customer` entity is flushed when:
- The transaction is committed.
- Before executing the consequent query, e.g. query data from the same table.
- Before executing the native query, e.g. query data from different table or the same table.

## Requirements
- Java 17+
- Maven
- (Optional) Docker, if using the provided `compose.yml` for database setup.

---

## Refreshing the Database
To refresh the database schema, you can use the following properties in your `application.properties` file:

```properties
spring.jpa.properties.hibernate.hbm2ddl.auto=create-drop
```

## Docker Support

If you want to run the application with a MySql database using Docker, you can use the provided `docker-compose.yml` file.
Make sure you have Docker installed and running, then execute:

```sh
docker-compose up
```
or uncomment these dependencies in `pom.xml`:

```xml
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-docker-compose</artifactId>
			<scope>runtime</scope>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>com.mysql</groupId>
			<artifactId>mysql-connector-j</artifactId>
			<scope>runtime</scope>
		</dependency>
```