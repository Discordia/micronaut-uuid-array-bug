package com.example;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
@Testcontainers
class ItemRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withInitScript("schema.sql");

    @Inject
    ItemRepository itemRepository;

    /**
     * BUG: This SHOULD work but fails with:
     * DataAccessException: Invalid UUID: [Ljava.util.UUID;@...
     */
    @Test
    void testBatchInsertWithUuidArray_shouldWorkButFails() {
        UUID[] ids = new UUID[]{UUID.randomUUID(), UUID.randomUUID()};

        // This should not throw - we expect Micronaut to bind UUID[] as a PG array
        itemRepository.batchInsertByIds(ids);

        for (UUID id : ids) {
            assertTrue(itemRepository.findById(id).isPresent());
        }
    }

    /**
     * BUG: This SHOULD work but fails because List<UUID> is expanded
     * for IN-clause style binding rather than as a single array parameter.
     */
    @Test
    void testBatchInsertWithList_shouldWorkButFails() {
        List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID());

        // This should not throw - we expect Micronaut to bind List<UUID> as a PG array
        itemRepository.batchInsertByIdsList(ids);

        ids.forEach(id -> assertTrue(itemRepository.findById(id).isPresent()));
    }

    /**
     * WORKAROUND: Passing a String in PostgreSQL array literal format works.
     */
    @Test
    void testBatchInsertWithStringWorkaround_works() {
        List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID());
        String pgArray = "{" + ids.stream().map(UUID::toString).collect(Collectors.joining(",")) + "}";

        itemRepository.batchInsertByIdsWorkaround(pgArray);

        ids.forEach(id -> assertTrue(itemRepository.findById(id).isPresent()));
    }
}
