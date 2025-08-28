# Entity Graphs Practical Guide for Academia App

## 🎯 **Overview**

This guide shows you how to use Entity Graphs with your specific entities to optimize JPA queries and prevent N+1 problems in your academia application.

## 📋 **Your Current Entity Structure**

### **Core Entities with Relationships**

```
Usuario (abstract)
├── Administrador
├── Profesor
└── Alumno
    ├── classes (ManyToMany with Clase)
    ├── payments (OneToMany with Pago)
    └── submissions (OneToMany with EntregaEjercicio)

Clase (abstract)
├── Curso
├── Taller
├── students (ManyToMany with Alumno)
├── teachers (ManyToMany with Profesor)
├── exercises (OneToMany with Ejercicio)
└── material (ManyToMany with Material)

Ejercicio
├── clase (ManyToOne with Clase)
└── entregas (OneToMany with EntregaEjercicio)

Pago
├── alumno (ManyToOne with Alumno)
├── clase (ManyToOne with Clase)
└── items (ElementCollection with ItemPago)

EntregaEjercicio
├── alumno (ManyToOne with Alumno)
└── ejercicio (ManyToOne with Ejercicio)
```

## 🚀 **Available Entity Graphs**

### **1. Clase Entity Graphs**

```java
// Already defined in your Clase.java
@NamedEntityGraph(name = "Clase.withStudentsAndTeachers")
@NamedEntityGraph(name = "Clase.withExercises")
@NamedEntityGraph(name = "Clase.withAllRelationships")
```

**Use Cases:**
- `withStudentsAndTeachers`: Admin dashboard, class overview
- `withExercises`: Professor exercise management
- `withAllRelationships`: Detailed class reports

### **2. Alumno Entity Graphs**

```java
// Already defined in your Alumno.java
@NamedEntityGraph(name = "Alumno.withClasses")
@NamedEntityGraph(name = "Alumno.withAllRelationships")
```

**Use Cases:**
- `withClasses`: Student dashboard, enrollment view
- `withAllRelationships`: Student profile, comprehensive data

### **3. Ejercicio Entity Graphs**

```java
// Already defined in your Ejercicio.java
@NamedEntityGraph(name = "Ejercicio.withClase")
@NamedEntityGraph(name = "Ejercicio.withEntregas")
@NamedEntityGraph(name = "Ejercicio.withAllRelationships")
```

**Use Cases:**
- `withClase`: Exercise details with class info
- `withEntregas`: Exercise grading, submission review
- `withAllRelationships`: Complete exercise analysis

### **4. Pago Entity Graph**

```java
// Already defined in your Pago.java
@NamedEntityGraph(name = "Pago.withItems")
```

**Use Cases:**
- `withItems`: Payment details, invoice generation

## 💡 **How to Use Entity Graphs**

### **Step 1: Add Repository Methods**

Add these methods to your repositories:

```java
// In RepositorioClase.java
@EntityGraph(value = "Clase.withStudentsAndTeachers")
List<Clase> findAllForDashboard();

@EntityGraph(value = "Clase.withExercises")
List<Clase> findAllForExerciseManagement();

@EntityGraph(value = "Clase.withAllRelationships")
Optional<Clase> findByIdWithAllRelationships(@Param("claseId") Long claseId);

@EntityGraph(value = "Clase.withStudentsAndTeachers")
@Query("SELECT c FROM Clase c WHERE c.difficulty = :difficulty")
List<Clase> findByDifficultyWithRelationships(@Param("difficulty") EDificultad difficulty);
```

```java
// In RepositorioAlumno.java
@EntityGraph(value = "Alumno.withClasses")
Optional<Alumno> findByIdWithClasses(@Param("alumnoId") Long alumnoId);

@EntityGraph(value = "Alumno.withAllRelationships")
Optional<Alumno> findByIdWithAllRelationships(@Param("alumnoId") Long alumnoId);
```

```java
// In RepositorioEjercicio.java
@EntityGraph(value = "Ejercicio.withEntregas")
List<Ejercicio> findByClaseIdWithEntregas(@Param("claseId") Long claseId);

@EntityGraph(value = "Ejercicio.withAllRelationships")
Optional<Ejercicio> findByIdWithAllRelationships(@Param("ejercicioId") Long ejercicioId);
```

```java
// In RepositorioPago.java
@EntityGraph(value = "Pago.withItems")
List<Pago> findByAlumnoIdWithItems(@Param("alumnoId") Long alumnoId);

@EntityGraph(value = "Pago.withItems")
Optional<Pago> findByIdWithItems(@Param("pagoId") Long pagoId);
```

### **Step 2: Add Service Methods**

Add optimized service methods:

```java
// In ServicioClase.java
@Transactional(readOnly = true)
public List<DTOClase> obtenerClasesParaDashboard() {
    return repositorioClase.findAllForDashboard()
            .stream()
            .map(DTOClase::new)
            .collect(Collectors.toList());
}

@Transactional(readOnly = true)
public DTOClase obtenerClaseConDetalles(Long claseId) {
    Clase clase = repositorioClase.findByIdWithAllRelationships(claseId)
            .orElseThrow(() -> new EntidadNoEncontradaException("Clase no encontrada"));
    return new DTOClase(clase);
}

@Transactional(readOnly = true)
public List<DTOClase> obtenerClasesPorDificultadConRelaciones(EDificultad dificultad) {
    return repositorioClase.findByDifficultyWithRelationships(dificultad)
            .stream()
            .map(DTOClase::new)
            .collect(Collectors.toList());
}
```

```java
// In ServicioAlumno.java
@Transactional(readOnly = true)
public DTOAlumno obtenerAlumnoConClases(Long alumnoId) {
    Alumno alumno = repositorioAlumno.findByIdWithClasses(alumnoId)
            .orElseThrow(() -> new EntidadNoEncontradaException("Alumno no encontrado"));
    return new DTOAlumno(alumno);
}

@Transactional(readOnly = true)
public DTOAlumno obtenerAlumnoConTodo(Long alumnoId) {
    Alumno alumno = repositorioAlumno.findByIdWithAllRelationships(alumnoId)
            .orElseThrow(() -> new EntidadNoEncontradaException("Alumno no encontrado"));
    return new DTOAlumno(alumno);
}
```

```java
// In ServicioEjercicio.java
@Transactional(readOnly = true)
public List<DTOEjercicio> obtenerEjerciciosConEntregas(Long claseId) {
    return repositorioEjercicio.findByClaseIdWithEntregas(claseId)
            .stream()
            .map(DTOEjercicio::new)
            .collect(Collectors.toList());
}

@Transactional(readOnly = true)
public DTOEjercicio obtenerEjercicioConTodo(Long ejercicioId) {
    Ejercicio ejercicio = repositorioEjercicio.findByIdWithAllRelationships(ejercicioId)
            .orElseThrow(() -> new EntidadNoEncontradaException("Ejercicio no encontrado"));
    return new DTOEjercicio(ejercicio);
}
```

```java
// In ServicioPago.java
@Transactional(readOnly = true)
public List<DTOPago> obtenerPagosConItems(Long alumnoId) {
    return repositorioPago.findByAlumnoIdWithItems(alumnoId)
            .stream()
            .map(DTOPago::new)
            .collect(Collectors.toList());
}

@Transactional(readOnly = true)
public DTOPago obtenerPagoConItems(Long pagoId) {
    Pago pago = repositorioPago.findByIdWithItems(pagoId)
            .orElseThrow(() -> new EntidadNoEncontradaException("Pago no encontrado"));
    return new DTOPago(pago);
}
```

### **Step 3: Use in REST Controllers**

```java
// In ClaseRest.java
@GetMapping("/dashboard")
public ResponseEntity<List<DTOClase>> obtenerClasesParaDashboard() {
    List<DTOClase> clases = servicioClase.obtenerClasesParaDashboard();
    return ResponseEntity.ok(clases);
}

@GetMapping("/{id}/detalles")
public ResponseEntity<DTOClase> obtenerClaseConDetalles(@PathVariable Long id) {
    DTOClase clase = servicioClase.obtenerClaseConDetalles(id);
    return ResponseEntity.ok(clase);
}

@GetMapping("/dificultad/{dificultad}")
public ResponseEntity<List<DTOClase>> obtenerClasesPorDificultad(@PathVariable EDificultad dificultad) {
    List<DTOClase> clases = servicioClase.obtenerClasesPorDificultadConRelaciones(dificultad);
    return ResponseEntity.ok(clases);
}
```

```java
// In AlumnoRest.java
@GetMapping("/{id}/con-clases")
public ResponseEntity<DTOAlumno> obtenerAlumnoConClases(@PathVariable Long id) {
    DTOAlumno alumno = servicioAlumno.obtenerAlumnoConClases(id);
    return ResponseEntity.ok(alumno);
}

@GetMapping("/{id}/completo")
public ResponseEntity<DTOAlumno> obtenerAlumnoCompleto(@PathVariable Long id) {
    DTOAlumno alumno = servicioAlumno.obtenerAlumnoConTodo(id);
    return ResponseEntity.ok(alumno);
}
```

## 🎯 **Real-World Use Cases**

### **1. Admin Dashboard**
```java
// Load classes with student and teacher counts
@GetMapping("/admin/dashboard")
public ResponseEntity<List<DTOClase>> obtenerDashboardAdmin() {
    // Uses Clase.withStudentsAndTeachers
    List<DTOClase> clases = servicioClase.obtenerClasesParaDashboard();
    return ResponseEntity.ok(clases);
}
```

### **2. Professor Exercise Management**
```java
// Load classes with exercises for grading
@GetMapping("/profesor/ejercicios")
public ResponseEntity<List<DTOClase>> obtenerClasesConEjercicios() {
    // Uses Clase.withExercises
    List<DTOClase> clases = servicioClase.obtenerClasesParaGestionEjercicios();
    return ResponseEntity.ok(clases);
}
```

### **3. Student Profile**
```java
// Load student with all their data
@GetMapping("/alumno/{id}/perfil")
public ResponseEntity<DTOAlumno> obtenerPerfilAlumno(@PathVariable Long id) {
    // Uses Alumno.withAllRelationships
    DTOAlumno alumno = servicioAlumno.obtenerAlumnoConTodo(id);
    return ResponseEntity.ok(alumno);
}
```

### **4. Exercise Grading**
```java
// Load exercise with all submissions
@GetMapping("/ejercicio/{id}/calificar")
public ResponseEntity<DTOEjercicio> obtenerEjercicioParaCalificar(@PathVariable Long id) {
    // Uses Ejercicio.withAllRelationships
    DTOEjercicio ejercicio = servicioEjercicio.obtenerEjercicioConTodo(id);
    return ResponseEntity.ok(ejercicio);
}
```

### **5. Payment Details**
```java
// Load payment with items for invoice
@GetMapping("/pago/{id}/factura")
public ResponseEntity<DTOPago> obtenerPagoParaFactura(@PathVariable Long id) {
    // Uses Pago.withItems
    DTOPago pago = servicioPago.obtenerPagoConItems(id);
    return ResponseEntity.ok(pago);
}
```

## ⚡ **Performance Benefits**

### **Before (N+1 Problem)**
```java
// This would cause multiple queries
List<Clase> clases = repositorioClase.findAll();
for (Clase clase : clases) {
    // Each access triggers a new query
    System.out.println("Estudiantes: " + clase.getStudents().size());  // Query #2
    System.out.println("Profesores: " + clase.getTeachers().size());   // Query #3
    System.out.println("Ejercicios: " + clase.getExercises().size());  // Query #4
    // ... more queries for each class
}
```

### **After (Optimized)**
```java
// Single optimized query with JOINs
@EntityGraph(value = "Clase.withAllRelationships")
List<Clase> clases = repositorioClase.findAllWithAllRelationships();
for (Clase clase : clases) {
    // No additional queries - data already loaded
    System.out.println("Estudiantes: " + clase.getStudents().size());  // No query
    System.out.println("Profesores: " + clase.getTeachers().size());   // No query
    System.out.println("Ejercicios: " + clase.getExercises().size());  // No query
}
```

## 🔧 **Dynamic Entity Graphs**

You can also create Entity Graphs on-the-fly:

```java
// In RepositorioClase.java
@EntityGraph(attributePaths = {"students", "teachers"})
List<Clase> findAllWithStudentsAndTeachers();

@EntityGraph(attributePaths = {"exercises.entregas"})
List<Clase> findAllWithExercisesAndSubmissions();

@EntityGraph(attributePaths = {"students", "exercises"})
List<Clase> findAllWithStudentsAndExercises();
```

## 📊 **Monitoring Performance**

Enable SQL logging to see the optimization:

```yaml
# In application-dev.yml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        generate_statistics: true
```

This will show you:
- The actual SQL queries generated
- Query execution statistics
- Performance metrics

## 🎯 **Best Practices**

### **1. Use Specific Entity Graphs**
```java
// Good - loads only what you need
@EntityGraph(value = "Clase.withStudentsAndTeachers")
List<Clase> findAllForDashboard();

// Avoid - loads everything when you only need students
@EntityGraph(value = "Clase.withAllRelationships")
List<Clase> findAllForDashboard();
```

### **2. Combine with Query Filters**
```java
@EntityGraph(value = "Clase.withExercises")
@Query("SELECT c FROM Clase c WHERE c.difficulty = :difficulty")
List<Clase> findByDifficultyWithExercises(@Param("difficulty") EDificultad difficulty);
```

### **3. Use in Read-Only Transactions**
```java
@Transactional(readOnly = true)
public List<DTOClase> obtenerClasesParaDashboard() {
    return repositorioClase.findAllForDashboard()
            .stream()
            .map(DTOClase::new)
            .collect(Collectors.toList());
}
```

## 🚀 **Quick Start Checklist**

### **✅ Phase 1: Repository Methods**
- [ ] Add Entity Graph methods to RepositorioClase
- [ ] Add Entity Graph methods to RepositorioAlumno
- [ ] Add Entity Graph methods to RepositorioEjercicio
- [ ] Add Entity Graph methods to RepositorioPago

### **✅ Phase 2: Service Methods**
- [ ] Add optimized service methods to ServicioClase
- [ ] Add optimized service methods to ServicioAlumno
- [ ] Add optimized service methods to ServicioEjercicio
- [ ] Add optimized service methods to ServicioPago

### **✅ Phase 3: REST Controllers**
- [ ] Add new endpoints using optimized methods
- [ ] Test performance improvements
- [ ] Monitor SQL queries

## 📋 **Summary**

Entity Graphs provide:
- ✅ **Performance optimization** - Prevents N+1 queries
- ✅ **Flexible loading** - Choose what to load for each use case
- ✅ **Easy implementation** - Just add @EntityGraph annotations
- ✅ **No breaking changes** - Works with existing code
- ✅ **Spring Boot integration** - Seamless with your current setup

Start with the repository methods and gradually add service and controller methods as needed!
