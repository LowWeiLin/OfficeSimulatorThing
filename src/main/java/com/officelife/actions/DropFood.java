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

        Coords locationToDrop = locationOffsetByOne(currentLocation);

        actor.inventory().remove(food);
        state.world.itemsAtLocation(locationToDrop).add(food);

        return true;
    }

    private Coords locationOffsetByOne(Coords location) {
        // TODO random choose one direction
        return new Coords(location.x, location.y - 1);
    }

    @Override
    public String toString() {
        return "Drop Food ";
    }
}
