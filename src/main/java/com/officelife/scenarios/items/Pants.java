package com.officelife.scenarios.items;

import java.util.UUID;

/**
 * In-game currency
 */
public class Pants implements Item {
    private final String id;

    public Pants() {
        this.id = UUID.randomUUID().toString();
    }

    @Override
    public char[][] textRepresentation() {
        return new char[][] {{'V'}};
    }

    @Override
    public String id() {
        return "Pants " + this.id;
    }

    @Override
    public String toString() {
        return String.format("Pants %s", id);
    }
}
