package com.officelife.actions.prerequisite;

import com.officelife.Coords;
import com.officelife.World;
import com.officelife.actions.Action;
import com.officelife.actors.Actor;

/**
 *
 */
public class LocationBeside {
    Coords first;
    Coords second;

    public LocationBeside(Coords first, Coords second) {
        this.first = first;
        this.second = second;
    }

    public LocationBeside(Actor first, Actor second, World world) {
        this(
                world.actorLocation(first)
                        .orElseThrow(() -> new RuntimeException("first actor's location cannot be found in the world " + first.id())),
                world.actorLocation(second)
                        .orElseThrow(() -> new RuntimeException("second actor's location cannot be found in the world " + second.id()))
        );
    }

    public boolean satisfied() {
        if (first.equals(second)) {
            return false;
        }
        return Math.abs(first.x - second.x) <= 1 && first.y == second.y
                || Math.abs(first.y - second.y) <= 1 && first.x == second.x;
    }

}
