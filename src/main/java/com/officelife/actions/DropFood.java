package com.officelife.actions;

import com.officelife.Coords;
import com.officelife.actors.Actor;
import com.officelife.goals.State;
import com.officelife.items.Food;

import java.util.Optional;

/**
 *
 */
public class DropFood extends Action {

    public DropFood(State state) {
        super(state);
    }

    @Override
    public boolean accept() {
        Actor actor = state.actor;

        Optional<Food> maybeFood = actor.inventory().stream()
                .filter(item -> item instanceof Food)
                .findFirst()
                .map(item -> (Food) item);

        if (!maybeFood.isPresent()) {
            return false;
        }

        Food food = maybeFood.get();
        Coords currentLocation = state.world.actorLocation(actor)
                .orElseThrow(() -> new RuntimeException("Actor no longer has a location in the world but took an action!"));

        actor.inventory().remove(food);
        state.world.itemsAtLocation(currentLocation).add(food);

        return true;
    }

    @Override
    public String toString() {
        return "Drop Food ";
    }
}
