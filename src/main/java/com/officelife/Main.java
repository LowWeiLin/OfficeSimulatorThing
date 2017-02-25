package com.officelife;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.officelife.actions.Action;
import com.officelife.actors.Actor;
import com.officelife.actors.Person;
import com.officelife.items.Coffee;
import com.officelife.items.Item;
import com.officelife.ui.Renderer;

public class Main {

    private static final boolean RENDER_TEXT = false;

    private void update(World state, Renderer renderer) {
        renderWorld(state, renderer);

        Map<String, Boolean> actionResults = new HashMap<>();

        List<Actor> actors = new ArrayList<>(state.actors.values());

        // Ensure that actions do not rely on the ordering of actors
        Collections.shuffle(actors);

        for (Actor actor : actors) {
            Action action;
            if (actionResults.containsKey(actor.id())) {
                action = actor.act(state, actionResults.get(actor.id()));
            } else {
                action = actor.act(state);
            }

            System.out.printf("%s: %s\n", actor.id(), action);

            actionResults.put(actor.id(), action.accept());
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
        String id = "Food guy";
        Actor coffeeDrinker = new Person(id);

        Coords origin = new Coords(0, 0);
      state.actorLocations.put(origin, id);
        state.actors.put(coffeeDrinker.id(), coffeeDrinker);

        Item coffee = new Coffee();
        Coords coffeeLocation = new Coords(origin.x + 1, origin.y - 1);

        state.itemLocations.put(coffeeLocation, coffee.id());
        state.items.put(coffee.id(), coffee);
        return state;
    }

    private void init() throws IOException {
        Renderer renderer = new Renderer();

        // state is closed over
        World state = initWorld();
        new Timer(renderer.getGUI(), () -> update(state, renderer), 6);

        renderer.getGUI().start();
    }

    public static void main(String[] args) throws IOException {
        new Main().init();
    }
}
