package com.example;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class ItemRepositoryTest {


    @Inject
    ItemRepository itemRepository;

    /**
     * BUG: This SHOULD work but fails with:
     * DataAccessException: Invalid UUID: [Ljava.util.UUID;@...
     */
    @Test
    void testBatchInsertWithUuidArray_shouldWorkButFails() {
        UUID[] ids = new UUID[]{UUID.randomUUID(), UUID.randomUUID()};

        // Demonstrates the bug: Micronaut cannot bind UUID[] as a PG array
        assertThrows(Exception.class, () -> itemRepository.batchInsertByIds(ids));
    }

    /**
     * BUG: This SHOULD work but fails because List<UUID> is expanded
     * for IN-clause style binding rather than as a single array parameter.
     */
    @Test
    void testBatchInsertWithList_shouldWorkButFails() {
        List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID());

        // Demonstrates the bug: Micronaut expands List for IN-clause style binding
        assertThrows(Exception.class, () -> itemRepository.batchInsertByIdsList(ids));
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
