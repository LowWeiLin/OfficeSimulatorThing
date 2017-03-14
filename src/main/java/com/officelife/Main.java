package com.officelife;

import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.officelife.actions.Action;
import com.officelife.actors.Actor;
import com.officelife.actors.Person;
import com.officelife.items.Coffee;
import com.officelife.items.Item;
import com.officelife.ui.Renderer;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private boolean paused = false;

    private void update(World state) {
        Map<String, Boolean> actionResults = new HashMap<>();

        List<Actor> actors = new ArrayList<>(state.actors());

        // Ensure that actions do not rely on the ordering of actors
        Collections.shuffle(actors);

        for (Actor actor : actors) {
            Action action;
            if (actionResults.containsKey(actor.id())) {
                action = actor.act(state, actionResults.get(actor.id()));
            } else {
                action = actor.act(state);
            }

            logger.debug("{}: {}", actor.id(), action);

            actionResults.put(actor.id(), action.accept());
        }

        for (Actor actor : actors) {
            if (actor.isDead()) {

                Coords location = state.actorLocation(actor).get();
                state.removeActor(actor);
                // TODO handle item drops
            }
        }
    }

    private static World initWorld() {
        World state = new World();
        String foodGuyId = "Food guy";
        Coords origin = new Coords(0, 0);
        putActor(state, foodGuyId, origin, 15, 15, 5);

        putActor(state, "Talking guy", new Coords(0, 1), 15, 15, 5);
//        putActorWithItems(state, "Talking guy", new Coords(0, 1), 10, 1, 10, new Coffee());

        Item coffee = new Coffee();
        Coords coffeeLocation = new Coords(origin.x + 1, origin.y - 1);

        List<Item> items = new ArrayList<>();
        items.add(coffee);
        state.itemsAtLocation(coffeeLocation).addAll(items);

        return state;
    }

    private static void putActor(World state, String personId, Coords coords ) {
        Actor person = new Person(personId);
        state.actorLocations.put(coords, person);
    }

    private static void putActor(
            World state, String personId, Coords coords, int physiology, int belonging, int energy) {
        Actor person = new Person(personId, physiology, belonging, energy);
        state.actorLocations.put(coords, person);
    }

    private static void putActorWithItems(
            World state, String personId, Coords coords, int physiology, int belonging, int energy,
            Item... items) {
        Actor person = new Person(personId, physiology, belonging, energy);
        state.actorLocations.put(coords, person);

        for (Item item : items) {
            person.addItem(item);
        }
    }

    private void gameLoop(Renderer renderer, World world) {
        if (!paused) {
            update(world);
            renderer.render(world);
        }
    }

    private boolean pause() {
        return paused = !paused;
    }

    /**
     * The main thread handles the UI and input.
     * A second thread (controlled by the Timer) periodically joins with the
     * main one to run game logic.
     *
     * State is only updated on the main thread, so there are no locks.
     */
    private void init() throws IOException {
        final Renderer renderer = new Renderer();

        final World world = initWorld();

        renderer.getGUI()
          .onRepl(new Scripting(world)::run)
          .onPause(this::pause);

        new Timer(() -> paused, () ->
          renderer.getGUI().runAndWait(() ->
            gameLoop(renderer, world)), 7);

        renderer.getGUI().start();
    }

    public static void main(String[] args) throws IOException {
        new Main().init();
    }
}
