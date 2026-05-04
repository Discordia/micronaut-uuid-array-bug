package com.example;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import io.micronaut.data.annotation.Query;

import java.util.List;
import java.util.UUID;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface ItemRepository extends CrudRepository<Item, UUID> {

    /**
     * BUG: This fails with "Invalid UUID: [Ljava.util.UUID;@..." because Micronaut
     * cannot bind UUID[] as a PostgreSQL array parameter.
     */
    @Query("""
        INSERT INTO item (id, name, created_at)
        SELECT ids.id, 'batch', now() FROM unnest(cast(:ids AS uuid[])) AS ids(id)
        ON CONFLICT (id) DO NOTHING
    """)
    void batchInsertByIds(UUID[] ids);

    /**
     * Also fails - Micronaut expands List for IN-clause style binding,
     * not as a single array parameter.
     */
    @Query("""
        INSERT INTO item (id, name, created_at)
        SELECT ids.id, 'batch', now() FROM unnest(cast(:ids AS uuid[])) AS ids(id)
        ON CONFLICT (id) DO NOTHING
    """)
    void batchInsertByIdsList(List<UUID> ids);

    /**
     * WORKAROUND: Pass as String in PostgreSQL array literal format "{uuid1,uuid2,...}"
     */
    @Query("""
        INSERT INTO item (id, name, created_at)
        SELECT ids.id, 'batch', now() FROM unnest(cast(:ids AS uuid[])) AS ids(id)
        ON CONFLICT (id) DO NOTHING
    """)
    void batchInsertByIdsWorkaround(String ids);
}

