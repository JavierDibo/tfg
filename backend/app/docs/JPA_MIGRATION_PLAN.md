# JPA Migration Plan - Complete Backend Migration

## Overview

This document outlines the comprehensive plan to migrate the entire backend from the current mixed approach (ID-based + JPA relationships) to pure JPA relationships.

## Current State Analysis

### ID-based Relationships (To Migrate)
- **Clase**: `studentIds`, `teacherIds`, `exerciseIds`
- **Alumno**: `classIds`, `paymentIds`, `submissionIds`
- **Profesor**: `classIds`
- **Pago**: `alumnoId`, `classId`
- **EntregaEjercicio**: `alumnoEntreganteId`, `ejercicioId`
- **Ejercicio**: `classId`

### Existing JPA Relationships (Keep)
- **Clase**: `material` (ManyToMany with Material)
- **Ejercicio**: `entregas` (OneToMany with EntregaEjercicio)
- **EntregaEjercicio**: `ejercicio` (ManyToOne with Ejercicio)
- **Pago**: `items` (ElementCollection with ItemPago)

## Migration Strategy

### Phase 1: Database Schema Preparation

#### 1.1 Create Migration Scripts
```sql
-- Create new junction tables for Many-to-Many relationships
CREATE TABLE IF NOT EXISTS clase_alumno (
    clase_id BIGINT NOT NULL,
    alumno_id BIGINT NOT NULL,
    PRIMARY KEY (clase_id, alumno_id),
    FOREIGN KEY (clase_id) REFERENCES clases(id) ON DELETE CASCADE,
    FOREIGN KEY (alumno_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS clase_profesor (
    clase_id BIGINT NOT NULL,
    profesor_id BIGINT NOT NULL,
    PRIMARY KEY (clase_id, profesor_id),
    FOREIGN KEY (clase_id) REFERENCES clases(id) ON DELETE CASCADE,
    FOREIGN KEY (profesor_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Add foreign key columns for One-to-Many relationships
ALTER TABLE ejercicios ADD COLUMN IF NOT EXISTS clase_entity_id BIGINT;
ALTER TABLE ejercicios ADD CONSTRAINT fk_ejercicio_clase 
    FOREIGN KEY (clase_entity_id) REFERENCES clases(id);

ALTER TABLE pagos ADD COLUMN IF NOT EXISTS alumno_entity_id BIGINT;
ALTER TABLE pagos ADD CONSTRAINT fk_pago_alumno 
    FOREIGN KEY (alumno_entity_id) REFERENCES usuarios(id);

ALTER TABLE pagos ADD COLUMN IF NOT EXISTS clase_entity_id BIGINT;
ALTER TABLE pagos ADD CONSTRAINT fk_pago_clase 
    FOREIGN KEY (clase_entity_id) REFERENCES clases(id);

ALTER TABLE entregas_ejercicio ADD COLUMN IF NOT EXISTS alumno_entity_id BIGINT;
ALTER TABLE entregas_ejercicio ADD CONSTRAINT fk_entrega_alumno 
    FOREIGN KEY (alumno_entity_id) REFERENCES usuarios(id);
```

#### 1.2 Data Migration Scripts
```sql
-- Migrate existing ID-based relationships to new JPA relationships
INSERT INTO clase_alumno (clase_id, alumno_id)
SELECT DISTINCT c.id, CAST(ca.alumno_id AS BIGINT)
FROM clases c
JOIN clase_alumnos ca ON c.id = ca.clase_id
WHERE ca.alumno_id ~ '^[0-9]+$';

INSERT INTO clase_profesor (clase_id, profesor_id)
SELECT DISTINCT c.id, CAST(cp.profesor_id AS BIGINT)
FROM clases c
JOIN clase_profesores cp ON c.id = cp.clase_id
WHERE cp.profesor_id ~ '^[0-9]+$';

-- Update foreign key columns
UPDATE ejercicios SET clase_entity_id = CAST(class_id AS BIGINT) 
WHERE class_id ~ '^[0-9]+$';

UPDATE pagos SET alumno_entity_id = CAST(alumno_id AS BIGINT) 
WHERE alumno_id ~ '^[0-9]+$';

UPDATE pagos SET clase_entity_id = CAST(class_id AS BIGINT) 
WHERE class_id ~ '^[0-9]+$';

UPDATE entregas_ejercicio SET alumno_entity_id = CAST(alumno_entregante_id AS BIGINT) 
WHERE alumno_entregante_id ~ '^[0-9]+$';
```

### Phase 2: Entity Updates

#### 2.1 Update Clase.java
```java
// Replace ID-based relationships with JPA relationships
@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE})
@JoinTable(
    name = "clase_alumno",
    joinColumns = @JoinColumn(name = "clase_id"),
    inverseJoinColumns = @JoinColumn(name = "alumno_id")
)
private List<Alumno> students = new ArrayList<>();

@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE})
@JoinTable(
    name = "clase_profesor",
    joinColumns = @JoinColumn(name = "clase_id"),
    inverseJoinColumns = @JoinColumn(name = "profesor_id")
)
private List<Profesor> teachers = new ArrayList<>();

@OneToMany(mappedBy = "clase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private List<Ejercicio> exercises = new ArrayList<>();

// Remove old ID-based fields
// private List<String> studentIds = new ArrayList<>();
// private List<String> teacherIds = new ArrayList<>();
// private List<String> exerciseIds = new ArrayList<>();
```

#### 2.2 Update Alumno.java
```java
@ManyToMany(mappedBy = "students", fetch = FetchType.LAZY)
private List<Clase> classes = new ArrayList<>();

@OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private List<Pago> payments = new ArrayList<>();

@OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private List<EntregaEjercicio> submissions = new ArrayList<>();

// Remove old ID-based fields
// private List<String> classIds = new ArrayList<>();
// private List<String> paymentIds = new ArrayList<>();
// private List<String> submissionIds = new ArrayList<>();
```

#### 2.3 Update Profesor.java
```java
@ManyToMany(mappedBy = "teachers", fetch = FetchType.LAZY)
private List<Clase> classes = new ArrayList<>();

// Remove old ID-based fields
// private List<String> classIds = new ArrayList<>();
```

#### 2.4 Update Ejercicio.java
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "clase_entity_id")
private Clase clase;

// Remove old ID-based field
// private String classId;
```

#### 2.5 Update Pago.java
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "alumno_entity_id")
private Alumno alumno;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "clase_entity_id")
private Clase clase;

// Remove old ID-based fields
// private String alumnoId;
// private Long classId;
```

#### 2.6 Update EntregaEjercicio.java
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "alumno_entity_id")
private Alumno alumno;

// Remove old ID-based fields
// private String alumnoEntreganteId;
// private String ejercicioId;
```

### Phase 3: Repository Updates

#### 3.1 Update RepositorioClase.java
```java
// Replace ID-based queries with JPA queries
@Query("SELECT c FROM Clase c JOIN c.students s WHERE s.id = :alumnoId")
List<Clase> findByAlumnoId(@Param("alumnoId") Long alumnoId);

@Query("SELECT c FROM Clase c JOIN c.teachers t WHERE t.id = :profesorId")
List<Clase> findByProfesorId(@Param("profesorId") Long profesorId);

@Query("SELECT c FROM Clase c JOIN c.exercises e WHERE e.id = :ejercicioId")
List<Clase> findByEjercicioId(@Param("ejercicioId") Long ejercicioId);

@Query("SELECT COUNT(c.students) FROM Clase c WHERE c.id = :claseId")
Integer countAlumnosByClaseId(@Param("claseId") Long claseId);

@Query("SELECT COUNT(c.teachers) FROM Clase c WHERE c.id = :claseId")
Integer countProfesoresByClaseId(@Param("claseId") Long claseId);

@Query("SELECT c FROM Clase c WHERE SIZE(c.students) = 0")
List<Clase> findClasesSinAlumnos();

@Query("SELECT c FROM Clase c WHERE SIZE(c.teachers) = 0")
List<Clase> findClasesSinProfesores();
```

#### 3.2 Update RepositorioAlumno.java
```java
@Query("SELECT a FROM Alumno a JOIN a.classes c WHERE c.id = :claseId")
List<Alumno> findByClaseId(@Param("claseId") Long claseId);

@Query("SELECT a FROM Alumno a WHERE SIZE(a.classes) = 0")
List<Alumno> findAlumnosSinClases();
```

#### 3.3 Update RepositorioProfesor.java
```java
@Query("SELECT p FROM Profesor p JOIN p.classes c WHERE c.id = :claseId")
List<Profesor> findByClaseId(@Param("claseId") Long claseId);

@Query("SELECT p FROM Profesor p WHERE SIZE(p.classes) = 0")
List<Profesor> findProfesoresSinClases();
```

### Phase 4: Service Layer Updates

#### 4.1 Update Entity Methods
```java
// Clase.java - Update relationship methods
public void addStudent(Alumno student) {
    if (!this.students.contains(student)) {
        this.students.add(student);
        student.getClasses().add(this);
    }
}

public void removeStudent(Alumno student) {
    this.students.remove(student);
    student.getClasses().remove(this);
}

public void addTeacher(Profesor teacher) {
    if (!this.teachers.contains(teacher)) {
        this.teachers.add(teacher);
        teacher.getClasses().add(this);
    }
}

public void removeTeacher(Profesor teacher) {
    this.teachers.remove(teacher);
    teacher.getClasses().remove(this);
}

public void addExercise(Ejercicio exercise) {
    if (!this.exercises.contains(exercise)) {
        this.exercises.add(exercise);
        exercise.setClase(this);
    }
}

public void removeExercise(Ejercicio exercise) {
    this.exercises.remove(exercise);
    exercise.setClase(null);
}
```

#### 4.2 Update Service Methods
```java
// ServicioClase.java - Update enrollment methods
@Transactional(isolation = Isolation.SERIALIZABLE)
public DTORespuestaEnrollment inscribirseEnClase(Long claseId) {
    Long alumnoId = securityUtils.getCurrentUserId();
    
    Clase clase = repositorioClase.findById(claseId).orElse(null);
    ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);
    
    Alumno alumno = repositorioAlumno.findById(alumnoId).orElse(null);
    ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", alumnoId);
    
    // Check if already enrolled
    if (clase.getStudents().contains(alumno)) {
        return DTORespuestaEnrollment.failure(alumnoId, claseId, 
            "El alumno ya está inscrito en esta clase", "ENROLLMENT");
    }
    
    clase.addStudent(alumno);
    repositorioClase.save(clase);
    
    return DTORespuestaEnrollment.success(alumnoId, claseId, 
        alumno.getFirstName(), clase.getTitle(), "ENROLLMENT");
}
```

### Phase 5: API Layer Updates

#### 5.1 Update DTOs
```java
// DTOClase.java - Update to use entity relationships
public DTOClase(Clase clase) {
    this.id = clase.getId();
    this.title = clase.getTitle();
    this.description = clase.getDescription();
    this.price = clase.getPrice();
    this.format = clase.getFormat();
    this.image = clase.getImage();
    this.difficulty = clase.getDifficulty();
    
    // Convert entity relationships to IDs
    this.studentIds = clase.getStudents().stream()
        .map(Alumno::getId)
        .map(String::valueOf)
        .collect(Collectors.toList());
    
    this.teacherIds = clase.getTeachers().stream()
        .map(Profesor::getId)
        .map(String::valueOf)
        .collect(Collectors.toList());
    
    this.exerciseIds = clase.getExercises().stream()
        .map(Ejercicio::getId)
        .map(String::valueOf)
        .collect(Collectors.toList());
}
```

### Phase 6: Testing Strategy

#### 6.1 Unit Tests
- Update all entity tests to use JPA relationships
- Test bidirectional relationship management
- Test cascade operations

#### 6.2 Integration Tests
- Test complete enrollment flow
- Test relationship queries
- Test performance with large datasets

#### 6.3 Database Tests
- Verify foreign key constraints
- Test cascade delete operations
- Verify data integrity

### Phase 7: Performance Optimization

#### 7.1 Lazy Loading Strategy
```java
// Use LAZY loading for most relationships
@ManyToMany(fetch = FetchType.LAZY)
@OneToMany(fetch = FetchType.LAZY)
@ManyToOne(fetch = FetchType.LAZY)

// Use EAGER only when necessary
@ManyToMany(fetch = FetchType.EAGER) // For Material relationship
```

#### 7.2 Query Optimization
```java
// Use JOIN FETCH for specific queries
@Query("SELECT DISTINCT c FROM Clase c " +
       "LEFT JOIN FETCH c.students " +
       "LEFT JOIN FETCH c.teachers " +
       "WHERE c.id = :claseId")
Clase findByIdWithRelationships(@Param("claseId") Long claseId);
```

## Migration Risks and Mitigation

### High Risk Areas
1. **Data Loss**: Complex migration scripts
2. **Performance**: N+1 query problems
3. **Breaking Changes**: API contract changes
4. **Transaction Management**: Complex relationship updates

### Mitigation Strategies
1. **Backup Strategy**: Complete database backup before migration
2. **Rollback Plan**: Keep old code as fallback
3. **Gradual Migration**: Migrate one entity at a time
4. **Extensive Testing**: Test all scenarios before production

## Timeline Estimate

- **Phase 1-2**: 2-3 weeks (Database + Entities)
- **Phase 3-4**: 2-3 weeks (Repositories + Services)
- **Phase 5**: 1 week (DTOs + APIs)
- **Phase 6**: 2 weeks (Testing)
- **Phase 7**: 1 week (Optimization)

**Total Estimated Time**: 8-10 weeks

## Conclusion

The migration to pure JPA relationships is **technically feasible** but **highly complex**. It requires:

1. **Significant development time** (8-10 weeks)
2. **Extensive testing** to ensure data integrity
3. **Careful planning** to avoid breaking changes
4. **Performance optimization** to maintain current response times

**Recommendation**: Consider if the benefits outweigh the risks and development time investment.
