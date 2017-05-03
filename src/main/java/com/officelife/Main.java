package com.officelife;

import java.io.IOException;
import java.util.*;

import com.officelife.core.Director;
import com.officelife.core.FirstWorld;
import com.officelife.core.Scenario;
import com.officelife.scenarios.items.Pants;
import com.officelife.scenarios.items.Food;
import com.officelife.scenarios.items.SharpStick;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.officelife.core.Actor;
import com.officelife.scenarios.Person;
import com.officelife.scenarios.items.Item;
import com.officelife.scenarios.wood.WoodcutterScenario;
import com.officelife.ui.Renderer;
import com.officelife.utility.*;
import com.officelife.utility.Timer;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private boolean paused = false;

    private void update(FirstWorld state) {
//        Map<String, Boolean> actionResults = new HashMap<>();

        List<Actor> actors = new ArrayList<>(state.actors());

        // Ensure that actions do not rely on the ordering of actors
        Collections.shuffle(actors);

        for (Actor actor : actors) {
//            Action action;
//            if (actionResults.containsKey(actor.id())) {
//                action = actor.act(state);
//            } else {
//                action =
                  actor.act(state);
//            }

//            logger.debug("{}: {}", actor.id(), action);

//            actionResults.put(actor.id(), action.actUpon());
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

        logger.info("===One turn has ended===");
        logger.info("Actors in the world = ");
        for (Actor actor : state.actors()) {
            logger.info("{} at {} : {}", actor.id(), state.actorLocation(actor).get(), actor.inventory());
        }
        logger.info("Unclaimed items in the world = ");
        for (Item item : state.items()) {
            logger.info("{} : {}", item.id());
        }
        logger.info("");
    }

    private static FirstWorld initWorld() {
        return new WoodcutterScenario().world();
    }

    private void gameLoop(Renderer renderer, FirstWorld world) {
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

        final FirstWorld world = initWorld();

        renderer.getGUI()
          .onRepl(new Scripting(world)::run)
          .onPause(this::pause);

        new Timer(() -> paused, () ->
          renderer.getGUI().runAndWait(() ->
            gameLoop(renderer, world)), 25);

        renderer.getGUI().start();
    }

    public static void main(String[] args) throws IOException {
        new Main().init();
    }
}
