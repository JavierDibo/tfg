For repositories, when using @EntityGraph with custom method names, we need to make sure to also add @Query annotations.

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
