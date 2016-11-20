package com.officelife;

import com.officelife.actions.Action;
import com.officelife.actors.Actor;
import com.officelife.actors.Person;
import com.officelife.items.CoffeeMachine;
import com.officelife.items.Item;

public class Main {
    private static final int NUM_ITERATIONS = 5;

    public static void main(String[] args) {

        World state = initWorld();

        for (int i = 0; i < NUM_ITERATIONS; i ++) {
            for (Actor actor : state.actors.values()) {
                Action action = actor.act(state);
                action.accept(state);
            }
        }
    }

    private static World initWorld() {
        World state = new World();
        Actor coffeeDrinker = new Person("Food guy");

        Pair<Integer, Integer> origin = new Pair<Integer, Integer>(0, 0);
        state.locationActor.put(origin, coffeeDrinker.id());
        state.actors.put(coffeeDrinker.id(), coffeeDrinker);

        Item coffeeMachine = new CoffeeMachine();
        Pair<Integer, Integer> coffeeLocation = new Pair<>(origin.first + 1, origin.second - 1);

        state.locationItems.put(coffeeLocation, coffeeMachine.id());
        state.items.put(coffeeMachine.id(), coffeeMachine);
        return state;
    }
}
