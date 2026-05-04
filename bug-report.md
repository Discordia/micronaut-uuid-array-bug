# Micronaut Data JDBC: Cannot bind UUID[] or List<UUID> as PostgreSQL array parameter in @Query

## Summary

When using `@Query` with a native PostgreSQL `unnest()` function that requires a `uuid[]` array parameter, Micronaut Data JDBC cannot bind `UUID[]` or `List<UUID>` as a single PostgreSQL array value. Instead, it attempts to bind it as a scalar UUID, resulting in a cast error.

## Expected Behavior

Passing a `UUID[]` or `List<UUID>` parameter to a `@Query` method should be bindable as a PostgreSQL array type when used with `unnest()` or similar array functions.

## Actual Behavior

- With `UUID[]`: Micronaut tries to bind the array as a single UUID, resulting in:
  ```
  io.micronaut.data.exceptions.DataAccessException: Invalid UUID: [Ljava.util.UUID;@142b0352
  ```
  The trace shows it calls `QueryStatement.setDynamic()` which routes to UUID handling but receives an array object.

- With `List<UUID>`: Micronaut expands the list for `IN`-clause style binding (individual positional parameters), which is incompatible with `unnest(:param::uuid[])` syntax.

## Steps to Reproduce

See the attached minimal reproducer project. The key code:

### Repository:
```java
@JdbcRepository(dialect = Dialect.POSTGRES)
public interface ItemRepository extends CrudRepository<Item, UUID> {

    @Query("""
        INSERT INTO item (id, name, created_at)
        SELECT ids.id, 'batch', now() FROM unnest(cast(:ids AS uuid[])) AS ids(id)
        ON CONFLICT (id) DO NOTHING
    """)
    void batchInsertByIds(UUID[] ids);
}
```

### Test:
```java
UUID[] ids = new UUID[]{UUID.randomUUID(), UUID.randomUUID()};
itemRepository.batchInsertByIds(ids);
```

### Result:
```
io.micronaut.data.exceptions.DataAccessException: Invalid UUID: [Ljava.util.UUID;@<hash>
    at io.micronaut.data.runtime.mapper.QueryStatement.setDynamic(QueryStatement.java:130)
    at io.micronaut.data.jdbc.mapper.JdbcQueryStatement.setDynamic(JdbcQueryStatement.java:138)
    ...
```

## Workaround

Pass the UUIDs as a `String` in PostgreSQL array literal format and cast in SQL:

```java
// Repository
@Query("""
    INSERT INTO item (id, name, created_at)
    SELECT ids.id, 'batch', now() FROM unnest(cast(:ids AS uuid[])) AS ids(id)
    ON CONFLICT (id) DO NOTHING
""")
void batchInsertByIds(String ids);

// Caller
String pgArray = "{" + uuids.stream().map(UUID::toString).collect(Collectors.joining(",")) + "}";
itemRepository.batchInsertByIds(pgArray);
```

## Environment

- Micronaut Data: 4.x (tested with latest)
- Database: PostgreSQL 15+
- Driver: io.micronaut.data:micronaut-data-jdbc with org.postgresql:postgresql
- Java: 21+

## Notes

This is a common pattern for efficient batch operations (batch insert with ON CONFLICT, batch delete, etc.) that avoids N individual statements. Native PostgreSQL supports binding array parameters via `PreparedStatement.setArray()`, but Micronaut Data's `@Query` parameter binding does not appear to support this path.

