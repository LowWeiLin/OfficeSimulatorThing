package com.officelife.actors;

import com.officelife.*;
import com.officelife.actions.Action;
import com.officelife.actions.DoNothing;
import com.officelife.actions.UseCoffeeMachine;
import com.officelife.actions.Move;
import com.officelife.characteristics.Characteristic;
import com.officelife.items.CoffeeMachine;
import com.officelife.items.Item;

import java.util.*;

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

    @Override
    public Action act(World state) {
        Pair<Integer, Integer> actorLocation;
        try {
            actorLocation = state.actorLocation(id);
        } catch (Exception e) {
            throw new RuntimeException("Unable to get actor location", e);
        }

        Item thingWanted = determineItemToObtain();
        Pair<Integer, Integer> itemLocation;

        if (!state.items.containsKey(thingWanted.id())) {
            // the thing is no longer in the world
            return new DoNothing();
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

    private Item determineItemToObtain() {
        return new CoffeeMachine();
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
        if (inventory.containsKey(item.getClass())) {
            inventory.get(item.getClass()).add(item);
        }
    }

    @Override
    public void removeItem(Item item) {
        inventory.get(item.getClass()).remove(item);
    }
}
