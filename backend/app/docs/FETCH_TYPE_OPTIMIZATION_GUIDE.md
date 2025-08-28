# Fetch Type Optimization Guide for Spring Boot

## Overview
This guide provides recommendations for optimizing fetch types in your JPA relationships based on Spring Boot best practices and your specific use cases.

## Current Fetch Type Analysis

### ✅ **Well-Configured Relationships**

#### 1. Material Relationship (EAGER)
```java
// In Clase.java - CORRECT
@ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
@JoinTable(name = "clase_material", ...)
private List<Material> material = new ArrayList<>();
```
**Rationale:** Materials are typically needed immediately when displaying class information.

#### 2. Most Relationships (LAZY)
```java
// All other relationships use LAZY - CORRECT
@ManyToMany(fetch = FetchType.LAZY)
@OneToMany(mappedBy = "...", fetch = FetchType.LAZY)
@ManyToOne(fetch = FetchType.LAZY)
```
**Rationale:** Prevents N+1 query problems and improves performance.

## Recommended Optimizations

### 1. **Use Entity Graphs for Specific Use Cases**

Instead of changing fetch types globally, use `@EntityGraph` for specific scenarios:

```java
// In RepositorioClase.java
@EntityGraph(attributePaths = {"students", "teachers", "material"})
List<Clase> findAllWithDetails();

@EntityGraph(attributePaths = {"exercises"})
List<Clase> findAllWithExercises();

@EntityGraph(attributePaths = {"students"})
List<Clase> findAllWithStudents();
```

### 2. **Optimize Pago Entity Graph**

Your current Named Entity Graph is good, but consider expanding it:

```java
// In Pago.java - Enhanced Entity Graph
@NamedEntityGraph(
    name = "Pago.withItemsAndAlumno",
    attributeNodes = {
        @NamedAttributeNode("items"),
        @NamedAttributeNode("alumno"),
        @NamedAttributeNode("clase")
    }
)
```

### 3. **Add Specific Fetch Methods**

Create repository methods for common fetch patterns:

```java
// In RepositorioAlumno.java
@Query("SELECT DISTINCT a FROM Alumno a " +
       "LEFT JOIN FETCH a.classes " +
       "LEFT JOIN FETCH a.payments " +
       "WHERE a.id = :alumnoId")
Optional<Alumno> findByIdWithAllRelationships(@Param("alumnoId") Long alumnoId);

@Query("SELECT a FROM Alumno a " +
       "LEFT JOIN FETCH a.classes " +
       "WHERE a.id = :alumnoId")
Optional<Alumno> findByIdWithClasses(@Param("alumnoId") Long alumnoId);
```

### 4. **Consider EAGER for Critical Relationships**

#### Students in Clase (for Admin Views)
```java
// Consider EAGER for admin dashboard views
@ManyToMany(fetch = FetchType.EAGER)
@JoinTable(name = "clase_alumno", ...)
private List<Alumno> students = new ArrayList<>();
```

**Alternative:** Use Entity Graph instead:
```java
@EntityGraph(attributePaths = {"students"})
List<Clase> findAllForAdminDashboard();
```

## Performance Considerations

### 1. **N+1 Query Prevention**

Your current LAZY configuration helps, but monitor for:
- Multiple entity loads in loops
- LazyInitializationException in detached entities

### 2. **Batch Size Optimization**

Add to `application.yml`:
```yaml
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 20
        jdbc:
          batch_size: 20
```

### 3. **Query Optimization**

Use `@Query` with `JOIN FETCH` for specific scenarios:

```java
// In RepositorioEjercicio.java
@Query("SELECT e FROM Ejercicio e " +
       "LEFT JOIN FETCH e.entregas " +
       "WHERE e.clase.id = :claseId")
List<Ejercicio> findByClaseIdWithEntregas(@Param("claseId") Long claseId);
```

## Recommended Fetch Type Strategy

### **Keep Current Configuration**
- ✅ **LAZY** for most relationships (current)
- ✅ **EAGER** for Material (current)
- ✅ **Named Entity Graph** for Pago (current)

### **Add Entity Graphs for Common Patterns**

```java
// In Clase.java
@NamedEntityGraph(
    name = "Clase.withStudentsAndTeachers",
    attributeNodes = {
        @NamedAttributeNode("students"),
        @NamedAttributeNode("teachers"),
        @NamedAttributeNode("material")
    }
)

@NamedEntityGraph(
    name = "Clase.withExercises",
    attributeNodes = {
        @NamedAttributeNode("exercises")
    }
)
```

### **Repository Method Patterns**

```java
// In RepositorioClase.java
@EntityGraph(value = "Clase.withStudentsAndTeachers")
List<Clase> findAllForDashboard();

@EntityGraph(value = "Clase.withExercises")
List<Clase> findAllForExerciseManagement();

@Query("SELECT c FROM Clase c " +
       "LEFT JOIN FETCH c.students " +
       "LEFT JOIN FETCH c.teachers " +
       "LEFT JOIN FETCH c.material " +
       "WHERE c.id = :claseId")
Optional<Clase> findByIdWithAllDetails(@Param("claseId") Long claseId);
```

## Monitoring and Tuning

### 1. **Enable SQL Logging (Development)**
```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
```

### 2. **Use Spring Boot Actuator**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### 3. **Monitor Query Performance**
- Use Hibernate Statistics
- Monitor slow queries
- Check for N+1 problems

## Best Practices Summary

1. **Default to LAZY** - Your current approach is correct
2. **Use Entity Graphs** - Instead of changing fetch types globally
3. **JOIN FETCH** - For specific query optimizations
4. **Monitor Performance** - Use logging and metrics
5. **Batch Fetching** - Configure appropriate batch sizes
6. **Consider Caching** - For frequently accessed data

## Implementation Priority

### **High Priority**
1. Add Entity Graphs for common fetch patterns
2. Optimize repository methods with JOIN FETCH
3. Configure batch fetch sizes

### **Medium Priority**
1. Add performance monitoring
2. Implement caching strategies
3. Optimize specific use cases

### **Low Priority**
1. Change existing fetch types
2. Add complex query optimizations
3. Implement advanced caching

## Conclusion

Your current fetch type configuration is well-designed. Focus on:
- Adding Entity Graphs for specific use cases
- Optimizing repository methods
- Monitoring performance
- Using JOIN FETCH for complex queries

This approach maintains flexibility while optimizing for common scenarios.
