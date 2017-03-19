package com.officelife.items;

import java.util.UUID;

/**
 * Gotta poke them all.
 */
public class SharpStick implements Weapon {
    private final String id;

    public SharpStick() {
        this.id = UUID.randomUUID().toString();
    }

    @Override
    public char[][] textRepresentation() {
        return new char[][] {{'W'}};
    }

    @Override
    public String id() {
        return "SharpStick_" + this.id;
    }

    @Override
    public int damage() {
        return 15;
    }

    @Override
    public String toString() {
        return String.format("Sharp Stick %s", id);
    }
}
