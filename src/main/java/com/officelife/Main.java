package com.officelife;

import java.io.IOException;
import java.util.*;

import com.officelife.actors.FruitTree;
import com.officelife.items.Pants;
import com.officelife.items.Food;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.officelife.actions.Action;
import com.officelife.actors.Actor;
import com.officelife.actors.Person;
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
                List<Item> items = new ArrayList<>(actor.inventory());
                actor.inventory().clear();
                state.removeActor(actor);

                state.itemsAtLocation(location)
                        .addAll(items);
            }
        }

        logger.debug("One turn has ended");
    }

    private static World initWorld() {
        World state = new World();
        String foodGuyId = "Food guy";
        Coords origin = new Coords(0, 0);
        putPersonWithItems(state, foodGuyId, origin, 15, 15, 5, new Pants());

        putPersonWithItems(state, "Talking guy", new Coords(0, 1), 15, 15, 5, new Pants());
//        putActorWithItems(state, "Talking guy", new Coords(0, 1), 10, 1, 10, new Food());


        putActor(state, new Coords(-1, -2), new FruitTree("Tree"));

        Item coffee = new Food();
        Coords coffeeLocation = new Coords(origin.x + 1, origin.y - 1);

        putItems(state, coffee, coffeeLocation);

        return state;
    }

    private static void putItems(World state, Item itemsToAdd, Coords location) {
        List<Item> items = new ArrayList<>();
        items.add(itemsToAdd);
        state.itemsAtLocation(location).addAll(items);
    }

    private static void putPerson(World state, String personId, Coords coords ) {
        Actor person = new Person(personId);
        state.actorLocations.put(coords, person);
    }

    private static void putPerson(
            World state, String personId, Coords coords, int physiology, int belonging, int energy) {
        Actor person = new Person(personId, physiology, belonging, energy);
        state.actorLocations.put(coords, person);
    }

    private static void putPersonWithItems(
            World state, String personId, Coords coords, int physiology, int belonging, int energy,
            Item... items) {
        Actor person = new Person(personId, physiology, belonging, energy);
        putActorWithItems(state, coords, person, items);
    }

    private static void putActor(World state, Coords coords, Actor person) {
        putActorWithItems(state, coords, person);
    }

    private static void putActorWithItems(World state, Coords coords, Actor person, Item... items) {
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
