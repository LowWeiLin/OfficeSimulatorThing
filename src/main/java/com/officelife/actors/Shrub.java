package com.officelife.actors;

import com.officelife.World;
import com.officelife.actions.Action;
import com.officelife.actions.Languish;
import com.officelife.goals.State;
import com.officelife.items.Item;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 */
public class Shrub implements Actor {

    private final String id;
    private final List<Item> inventory;

    public Shrub(String id, List<Item> inventory) {
        this.id = id;
        this.inventory = inventory;
    }

    @Override
    public char[][] textRepresentation() {
        return new char[0]['T'];
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Action act(World state, boolean succeeded) {
        if (ThreadLocalRandom.current().nextInt(0,100) > 50) {

        }
        return new Languish(new State(state, this));
    }

    @Override
    public List<Item> inventory() {
        return inventory;
    }

    @Override
    public void addItem(Item item) {

    }

    @Override
    public boolean isDead() {
        return false;
    }
}
