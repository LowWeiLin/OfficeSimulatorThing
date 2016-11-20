package com.officelife.items;

import java.util.UUID;

/**
 * This is a coffee.
 * @see CoffeeMachine
 */
public class Coffee implements Item {
    private final String id;

    public Coffee() {
        id = "Coffee_" + UUID.randomUUID().toString();
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public char[][] asciiRepresentation() {
        return new char[][] {{'C'}};
    }
}
