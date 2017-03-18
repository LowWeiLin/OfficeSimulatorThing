package com.officelife.items;

import java.util.UUID;

public class Food implements Item {
    private final String id;

    public Food() {
        id = "Food_" + UUID.randomUUID().toString();
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public char[][] textRepresentation() {
        return new char[][] {{'\u2615'}};
    }

    @Override
    public String toString() {
        return String.format("Food %s", id);
    }
}
