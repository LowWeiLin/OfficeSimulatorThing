package com.officelife.actors;

import java.util.*;

import com.officelife.World;
import com.officelife.actions.*;
import com.officelife.characteristics.Characteristic;
import com.officelife.common.Pair;
import com.officelife.items.Coffee;
import com.officelife.items.CoffeeMachine;
import com.officelife.items.Item;

public class Person implements Actor {
    private final String id;
    private final String name;

    private EnumMap<ActorState, Integer> needs;

    private Map<Class<? extends Item>, Collection<Item>> inventory;

    public Collection<Characteristic> characteristics;

    public Person(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;

        this.needs = new EnumMap<>(ActorState.class);
        for (ActorState need : ActorState.values()) {
            this.needs.put(need, 0);
        }

        this.inventory = new HashMap<>();
    }

    @Override
    public String id() {
        return id;
    }

    private Item determineItemToObtain() {
        if (!inventory.containsKey(Coffee.class)) {
            inventory.put(Coffee.class, new ArrayList<>());
        }
        return inventory.get(Coffee.class).stream()
            .findFirst()
            .orElse(new CoffeeMachine());
    }

    @Override
    public Action act(World state) {
        // TODO the whole method is wrong

        Pair<Integer, Integer> actorLocation;
        try {
            actorLocation = state.actorLocation(id);
        } catch (Exception e) {
            throw new RuntimeException("Unable to get actor location", e);
        }

        Item thingWanted = determineItemToObtain();
        Pair<Integer, Integer> itemLocation;

        boolean inventoryContainsItemType = this.inventory.containsKey(thingWanted.getClass());

        if (!state.items.containsKey(thingWanted.id())
            && !inventoryContainsItemType) {
            // the thing is no longer in the world
            return new DoNothing();
        }

        if (inventoryContainsItemType && thingWanted.getClass() == Coffee.class) {
            return new DrinkCoffee(this, (Coffee) thingWanted);
        }

        try {
            itemLocation = state.itemLocation(thingWanted.id());
        } catch (Exception e) {
            throw new RuntimeException("Unable to get item location", e);
        }

        if (itemLocation.equals(actorLocation)) {
            return new UseCoffeeMachine(this, (CoffeeMachine) thingWanted);
        }

        return new Move(this, Move.Direction.directionToMove(actorLocation, itemLocation));
    }


    @Override
    public void changeNeed(ActorState need, int value) {
        int current = needs.get(need);
        needs.put(need, current + value);

        System.err.println(need + " changed " + value);
        System.err.println(need + " is now " + needs.get(need));
    }

    @Override
    public void addItem(Item item) {
        Class<? extends Item> itemClass = item.getClass();
        if (!inventory.containsKey(itemClass)) {
            inventory.put(itemClass, new ArrayList<>());
        }
        inventory.get(itemClass).add(item);
    }

    @Override
    public void removeItem(Item item) {
        inventory.get(item.getClass()).remove(item);
    }

    @Override
    public char[][] asciiRepresentation() {
        return new char[][]{{'P'}};
    }
}
