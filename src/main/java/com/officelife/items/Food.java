package com.officelife.items;

import com.officelife.actors.Actor;
import com.officelife.actors.Person;

import java.util.UUID;

public class Food implements Consumable {
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
        return new char[][] {{'C'}};
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
}
