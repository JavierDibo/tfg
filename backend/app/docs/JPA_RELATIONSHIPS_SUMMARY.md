# JPA Entity Relationships Summary

## Overview
This document provides a comprehensive overview of all JPA relationships between the entity classes in the academia application.

## Inheritance Hierarchies

### 1. Usuario Hierarchy (Single Table Inheritance)
```java
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_usuario", discriminatorType = DiscriminatorType.STRING)
public abstract class Usuario
```

**Subclasses:**
- `Administrador` - `@DiscriminatorValue("ADMINISTRADOR")`
- `Profesor` - `@DiscriminatorValue("PROFESOR")`
- `Alumno` - `@DiscriminatorValue("ALUMNO")`

**Strategy:** Single Table Inheritance - All user types stored in one table with discriminator column.

### 2. Clase Hierarchy (Single Table Inheritance)
```java
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_clase", discriminatorType = DiscriminatorType.STRING)
public abstract class Clase
```

**Subclasses:**
- `Curso` - `@DiscriminatorValue("CURSO")` (with start/end dates)
- `Taller` - `@DiscriminatorValue("TALLER")` (with duration and specific date/time)

**Strategy:** Single Table Inheritance - All class types stored in one table with discriminator column.

## JPA Relationships

### Many-to-Many Relationships

#### 1. Clase ↔ Alumno (Students)
```java
// In Clase.java
@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(
    name = "clase_alumno",
    joinColumns = @JoinColumn(name = "clase_id"),
    inverseJoinColumns = @JoinColumn(name = "alumno_id")
)
private List<Alumno> students = new ArrayList<>();

// In Alumno.java
@ManyToMany(mappedBy = "students", fetch = FetchType.LAZY)
private List<Clase> classes = new ArrayList<>();
```

**Purpose:** Tracks which students are enrolled in which classes.

#### 2. Clase ↔ Profesor (Teachers)
```java
// In Clase.java
@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(
    name = "clase_profesor",
    joinColumns = @JoinColumn(name = "clase_id"),
    inverseJoinColumns = @JoinColumn(name = "profesor_id")
)
private List<Profesor> teachers = new ArrayList<>();

// In Profesor.java
@ManyToMany(mappedBy = "teachers", fetch = FetchType.LAZY)
private List<Clase> classes = new ArrayList<>();
```

**Purpose:** Tracks which teachers teach which classes.

#### 3. Clase ↔ Material
```java
// In Clase.java
@ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
@JoinTable(
    name = "clase_material",
    joinColumns = @JoinColumn(name = "clase_id"),
    inverseJoinColumns = @JoinColumn(name = "material_id")
)
private List<Material> material = new ArrayList<>();
```

**Purpose:** Associates teaching materials with classes. Uses EAGER fetching for immediate access.

### One-to-Many Relationships

#### 1. Clase → Ejercicio (Exercises)
```java
// In Clase.java
@OneToMany(mappedBy = "clase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private List<Ejercicio> exercises = new ArrayList<>();

// In Ejercicio.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "class_id")
private Clase clase;
```

**Purpose:** Each class can have multiple exercises assigned to it.

#### 2. Ejercicio → EntregaEjercicio (Submissions)
```java
// In Ejercicio.java
@OneToMany(mappedBy = "ejercicio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private List<EntregaEjercicio> entregas = new ArrayList<>();

// In EntregaEjercicio.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "ejercicio_id")
private Ejercicio ejercicio;
```

**Purpose:** Each exercise can have multiple student submissions.

#### 3. Alumno → EntregaEjercicio (Student Submissions)
```java
// In Alumno.java
@OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private List<EntregaEjercicio> submissions = new ArrayList<>();

// In EntregaEjercicio.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "alumno_id")
private Alumno alumno;
```

**Purpose:** Each student can have multiple exercise submissions.

#### 4. Alumno → Pago (Payments)
```java
// In Alumno.java
@OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private List<Pago> payments = new ArrayList<>();

// In Pago.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "alumno_id")
private Alumno alumno;
```

**Purpose:** Each student can have multiple payments.

#### 5. Clase → Pago (Class Payments)
```java
// In Clase.java (implicit through Pago entity)
// In Pago.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "clase_id")
private Clase clase;
```

**Purpose:** Each class can have multiple payments associated with it.

### Element Collections

#### 1. Pago → ItemPago (Payment Items)
```java
// In Pago.java
@ElementCollection(fetch = FetchType.LAZY)
@CollectionTable(name = "pago_items", joinColumns = @JoinColumn(name = "pago_id"))
private List<ItemPago> items = new ArrayList<>();

// In ItemPago.java
@Embeddable
public class ItemPago
```

**Purpose:** Each payment can have multiple items (products/services purchased).

#### 2. EntregaEjercicio → String (Submitted Files)
```java
// In EntregaEjercicio.java
@ElementCollection(fetch = FetchType.LAZY)
@CollectionTable(name = "entrega_archivos", joinColumns = @JoinColumn(name = "entrega_id"))
@Column(name = "archivo_path")
private List<String> archivosEntregados = new ArrayList<>();
```

**Purpose:** Each submission can have multiple file paths associated with it.

## Special Configurations

### Named Entity Graph
```java
// In Pago.java
@NamedEntityGraph(
    name = "Pago.withItems",
    attributeNodes = @NamedAttributeNode("items")
)
```

**Purpose:** Optimizes loading of Pago entities with their associated items.

### Fetch Types
- **LAZY (Default):** Most relationships use lazy loading for performance
- **EAGER:** Material relationship in Clase uses eager loading for immediate access

### Cascade Types
- **CascadeType.ALL:** Used for child entities (Ejercicio, EntregaEjercicio, Pago)
- **CascadeType.MERGE:** Used for Material relationship

## Database Tables Generated

### Main Tables
1. `usuarios` - Single table for all user types (Usuario, Administrador, Profesor, Alumno)
2. `clases` - Single table for all class types (Clase, Curso, Taller)
3. `ejercicios` - Exercise table
4. `entregas_ejercicio` - Exercise submissions table
5. `pagos` - Payments table
6. `materiales` - Materials table

### Junction Tables (Many-to-Many)
1. `clase_alumno` - Links classes and students
2. `clase_profesor` - Links classes and teachers
3. `clase_material` - Links classes and materials

### Collection Tables (Element Collections)
1. `pago_items` - Payment items
2. `entrega_archivos` - Submitted file paths

## Key Features

### Bidirectional Relationships
All Many-to-Many and One-to-Many relationships are properly maintained as bidirectional, with helper methods to manage both sides of the relationship.

### Validation
- All entities include proper validation annotations
- Business logic validation in entity methods
- Time-based validation for exercises and classes

### Security Integration
- Entities integrate with Spring Security through UserDetails interface
- Role-based access control through Role enum

### Payment Integration
- Stripe payment processing fields
- Payment status tracking
- Invoice generation capabilities

## Best Practices Implemented

1. **Lazy Loading:** Default fetch type for performance
2. **Cascade Operations:** Proper cascade configuration for data integrity
3. **Bidirectional Relationships:** Properly maintained on both sides
4. **Validation:** Comprehensive validation at entity level
5. **Inheritance:** Efficient single table inheritance strategy
6. **Named Queries:** Optimized loading with entity graphs
