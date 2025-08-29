This document provides a set of requirements for each type of class to follow. Each point includes a question. Answer ALL questions for the provided file(s) (if the file is a service all service questions, if it's a controller all controller questions, etc).

# Controllers

1. If needed make use of `util/ValidationUtils.java` as well as the `util/validation/` classes. ¿Is the controller using custom/private validation/security instead of the one provided by the utils?

2. Make sure the controller does not operate on multiple reponsibilites, we have a `EstadisticasRestController.java` for statistics, a `MatriculaRestController.java` for enrollments (if aplicable). ¿Does the controller expose endpoints that should be located in the Statistics or Matricula controller?

3. We should strive to be as RESTful as possible. ¿Is the controller aligned with RESTful standards?

4. Make sure we use body for PUT and POST operations, creating and using the proper DTOs for this operations. ¿Is the controller using path variables instead of body for PUT and POST?

5. Make sure to make search functions both pageable and not pageable. ¿Does the controller provide an endpoint to both obtain all data and paginated data for the same data?

5.1. Both the paginated and non paginated methods should include the same parameters search functions (including `SortDirection` and `SortBy`). The intent is to have the same functionality, just that one returns everything and the paginated one is paginated, that whould be the only differnce. The respective non-paginated method should add an `/all` to the endpoint.

6. Make sure to should expose as many of the useful Service methods we have to the API. ¿Is the controller exposing all the relevant business logic availible?

# Services

1. Make sure to use the validators in `util/ValidationUtils.java` as well as the `util/validation/` classes. ¿Is the service using custom/private validation/security instead of the one provided by the utils? 

1.1. In the case you need to create new validation methods or find existing validation in the service, please create/move them to the validation files. ¿Does the document contain validation/security functions that could be moved to the utils?

2. Make sure to analyze each individual function and make sure the service does not operate on multiple reponsibilites, we have a `ServicoEstadisticas.java` for statistics, a `ServicioMatricula.java` for enrollments (if aplicable). ¿Does the service include functions that should be located in the Statistics (counting, etc related) or Matricula (enrollment related) service?

3. Make sure the methods do not repeat reponsibilities. ¿Are there several methods that hold the same or very similar intentions in the service?

4. Make sure to make search functions both pageable and not pageable. ¿Does the service provide a function to both obtain all data and paginated data for the same data?

# Repositories

1. We need to use proper SPQL and JPQL for the queries. ¿Is the repository using custom/monolingual SQL querys?

2. We need to sanitize strings to include any wildcard of accents and cases. ¿Are we handling case and accents in any string that can include them?

2.1. For example the name Juan Fernández Sánchez needs to pop up when we search for "fern".

2. Make sure to use EntityGraph accordingly, if the original entity includes it. ¿Are we managing the EntityGraph features according to Spring Boots standards?

3. For repositories, when using @EntityGraph with custom method names, we need to make sure to also add @Query annotations.

[ ] Method name follows standard Spring Data patterns? → No @Query needed
[ ] Method name is custom/includes relationship hints? → Add @Query
[ ] Using @EntityGraph with custom method? → Always add @Query

## Hibernate 6 Parameter Type Inference Bug

**Error**: `function upper(bytea) does not exist` in PostgreSQL with Hibernate 6

**Root Cause**: Hibernate 6 has a bug where it can't properly infer SQL types for parameters that appear multiple times in a query, especially when the first usage is in an `IS NULL` check. This causes PostgreSQL to treat string parameters as `BYTEA` instead of `VARCHAR`/`TEXT`.

**Problematic Pattern**:
```sql
WHERE (:param IS NULL OR UPPER(column) LIKE UPPER(CONCAT('%', :param, '%')))
```

**Solution**: Flip the order of conditions so the parameter is used in a string operation first:
```sql
WHERE (UPPER(column) LIKE UPPER(CONCAT('%', :param, '%')) OR :param IS NULL)
```

**Why it works**: Hibernate can infer the correct SQL type (VARCHAR/TEXT) from the string operation before encountering the `IS NULL` check.

**Files affected**: Repository queries with multiple parameter usages in flexible search methods.

**References**: 
- Hibernate 6 bug: "Add support for setObject(, null)"
- Spring Data JPA hasn't adopted Hibernate 6's new `setParameter(String, Object, Class<P> type)` API yet
