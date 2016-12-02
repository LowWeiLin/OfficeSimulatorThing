package com.officelife.items;

import java.util.UUID;

/**
 * This should produce {@code Coffee}.
 * @see Coffee
 */
public class CoffeeMachine implements Item {
    @Override
    public String id() {
        return UUID.randomUUID().toString();
    }

    @Override
    public char[][] textRepresentation() {
        return new char[][] {{'C'}};
    }
}
