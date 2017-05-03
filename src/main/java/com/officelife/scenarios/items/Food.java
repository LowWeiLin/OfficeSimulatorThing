package com.officelife.scenarios.items;

import com.officelife.core.Actor;
import com.officelife.scenarios.Person;

import java.util.UUID;

public class Food implements Consumable {
    private final String id;

    public Food() {
        id = "Food_" + UUID.randomUUID().toString();
    }

    @Override
    public String toString() {
        return String.format("Food %s", id);
    }

    @Override
    public void consumedBy(Actor actor) {
        if (actor instanceof Person) {
            ((Person)actor).energy += 15;
        }
    }

    @Override
    public char[][] textRepresentation() {
        return new char[][] {{'C'}};
    }

    @Override
    public String id() {
        return id;
    }
}
