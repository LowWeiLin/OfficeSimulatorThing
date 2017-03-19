package com.officelife.goals.subgoals;

import com.officelife.Coords;
import com.officelife.World;
import com.officelife.actions.*;
import com.officelife.actions.prerequisite.LocationBeside;
import com.officelife.actors.Actor;
import com.officelife.goals.Goal;
import com.officelife.goals.Outcome;
import com.officelife.goals.State;
import com.officelife.goals.effects.Effect;
import com.officelife.goals.effects.TerminalAction;


import java.util.List;
import java.util.Optional;

/**
 * Ouch.
 */
public class Attack extends Goal {
    private enum Status {
        FINDING, COMPLETED
    }

    private Status status;

    private boolean failed = false;

    private Actor target;

    public Attack(Actor target) {
        this.target = target;
        this.status = Status.FINDING;
    }

    @Override
    public Outcome outcome() {
        if (failed) {
            return Outcome.FAILURE;
        }

        if (status == Status.COMPLETED) {
            return Outcome.SUCCESS;
        }

        return Outcome.CONTINUE;
    }

    @Override
    public Effect effect(State state) {
        switch (status) {


            case FINDING:

                Optional<List<Coords>> pathToTarget = state.world.actorLocation(target)
                    .flatMap(coords ->
                        state.world.findPath(
                            state.world.actorLocation(state.actor).get(),
                            new World.EndCoords(coords))
                    );

                if (!pathToTarget.isPresent()) {
                    failed = true;
                    return new TerminalAction(new Languish(state));
                }
                if (new LocationBeside(state.actor, target, state.world)
                        .satisfied()) {
                    status = Status.COMPLETED;

                    return new TerminalAction(new AttackSomeone(state, target));
                }

                return new TerminalAction(
                    new Move(
                        state,
                        Move.Direction.directionToMove(
                            state.world.actorLocation(state.actor).get(), pathToTarget.get().get(0)
                        )
                    )
                );

            default:
                return new TerminalAction(new Languish(state));
        }
    }


}
