package com.officelife.scenarios.wildling.ops;


import com.officelife.core.Actor;
import com.officelife.core.FirstWorld;
import com.officelife.core.WorldState;
import com.officelife.core.changes.Languish;
import com.officelife.scenarios.items.Food;
import com.officelife.scenarios.items.Item;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class FruitTree implements Actor {

    private final String id;
    private List<Item> inventory;

    public FruitTree(String id) {
        this.id = id;
    }

    @Override
    public char[][] textRepresentation() {
        return new char[0][];
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void act(FirstWorld state) {

        if (ThreadLocalRandom.current().nextInt(0,100) > 50) {
            if (this.inventory().stream().anyMatch(item -> item instanceof Food)) {
                new Languish().actUpon(new WorldState(state, this));
                return;
            }


        }

    }

    @Override
    public List<Item> inventory() {
        return null;
    }

    @Override
    public void addItem(Item item) {

    }

    @Override
    public boolean isDead() {
        return false;
    }
}
