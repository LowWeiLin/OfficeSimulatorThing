package com.officelife.items;

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
        return new char[][] {{'P'}};
    }

    @Override
    public String id() {
        return "Pants " + this.id;
    }

    @Override
    public String toString() {
        return String.format("Food %s", id);
    }
}
