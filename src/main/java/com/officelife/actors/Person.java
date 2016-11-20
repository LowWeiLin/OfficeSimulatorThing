package com.officelife.actors;

import com.officelife.*;
import com.officelife.actions.Action;
import com.officelife.actions.DoNothing;
import com.officelife.actions.UseCoffeeMachine;
import com.officelife.actions.Move;
import com.officelife.items.CoffeeMachine;
import com.officelife.items.Item;

import java.util.EnumMap;
import java.util.UUID;

public class Person implements Actor {
    private final String id;
    private final String name;

    private EnumMap<ActorState, Integer> needs;

    public Person(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;

        this.needs = new EnumMap<>(ActorState.class);
        for (ActorState need : ActorState.values()) {
            this.needs.put(need, 0);
        }
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Action act(World state) {
        Pair<Integer, Integer> location;
        try {
            location = state.actorLocation(id);
        } catch (Exception e) {
            throw new RuntimeException("Unable to get actor location", e);
        }

        Item thingWanted = new CoffeeMachine();
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

        if (itemLocation.equals(location)) {
            return new UseCoffeeMachine(this, (CoffeeMachine) thingWanted);
        }

        return new Move(this, Move.Direction.directionToMove(location, itemLocation));
    }



    @Override
    public void changeNeed(ActorState need, int value) {
        int current = needs.get(need);
        needs.put(need, current + value);

        System.err.println(need + " changed " + value);
        System.err.println(need + " is now " + needs.get(need));
    }
}
