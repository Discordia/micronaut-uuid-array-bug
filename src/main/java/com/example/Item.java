package com.example;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

import java.time.Instant;
import java.util.UUID;

@MappedEntity("item")
public record Item(
    @Id UUID id,
    String name,
    Instant createdAt
) {}

