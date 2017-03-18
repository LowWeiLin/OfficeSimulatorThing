package com.officelife.actors;

import com.officelife.World;
import com.officelife.actions.Action;
import com.officelife.actions.CreateFood;
import com.officelife.actions.DropFood;
import com.officelife.actions.Languish;
import com.officelife.goals.State;
import com.officelife.items.Food;
import com.officelife.items.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 */
public class FruitTree implements Actor {

    private final String id;
    private List<Item> inventory;

    public FruitTree(String id, List<Item> inventory) {
        this.id = id;
        this.inventory = inventory;
    }

    public FruitTree(String id) {
        this(id, new ArrayList<>());
    }

    @Override
    public char[][] textRepresentation() {
        return new char[][] {{'T'}};
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Action act(World world, boolean succeeded) {
        State state = new State(world, this);
        if (ThreadLocalRandom.current().nextInt(0,100) > 50) {
            if (this.inventory().stream().anyMatch(item -> item instanceof Food)) {
                return new DropFood(state);
            }
            return new CreateFood(state);
        }
        return new Languish(state);
    }

    @Override
    public List<Item> inventory() {
        return inventory;
    }

    @Override
    public void addItem(Item item) {
        // TODO should trees only carry a single food?
        this.inventory = new ArrayList<>();
        inventory.add(item);
    }

    @Override
    public boolean isDead() {
        return false;
    }
}
