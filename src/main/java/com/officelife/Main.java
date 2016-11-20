package com.officelife;

import java.io.IOException;
import java.util.concurrent.Executors;

import com.officelife.actions.Action;
import com.officelife.actors.Actor;
import com.officelife.actors.Person;
import com.officelife.common.Pair;
import com.officelife.items.CoffeeMachine;
import com.officelife.items.Item;
import com.officelife.ui.Renderer;

public class Main {

    private static final int NUM_ITERATIONS = 5;
    private static final boolean RENDER_TEXT = false;

    public static void main(String[] args) throws IOException {
        new Main().init();
    }

    private void init() throws IOException {
        Renderer renderer = new Renderer();
        Executors.newSingleThreadExecutor(r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        }).submit(() -> run(renderer));
        renderer.runGUI();
    }

    private void run(Renderer renderer) {
        World state = initWorld();

        for (int i = 0; i < NUM_ITERATIONS; i++) {
            renderWorld(state, renderer);
            for (Actor actor : state.actors.values()) {
                Action action = actor.act(state);
                action.accept(state);
            }
        }
    }

    private static void renderWorld(World state, Renderer renderer) {
        if (RENDER_TEXT) {
            System.out.println(renderer.renderText(state));
        } else {
            renderer.render(state);
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
