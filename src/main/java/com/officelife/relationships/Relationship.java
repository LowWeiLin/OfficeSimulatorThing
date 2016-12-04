package com.officelife.relationships;

import com.officelife.common.Pair;

import java.util.UUID;

public interface Relationship {
    UUID from(UUID agent);
    UUID to(UUID agent);
    boolean has(UUID a, UUID b);
}
