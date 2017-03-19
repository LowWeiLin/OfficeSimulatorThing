package com.officelife.goals.subgoals;

import com.officelife.Coords;
import com.officelife.actions.*;
import com.officelife.actors.Actor;
import com.officelife.actors.Person;
import com.officelife.goals.Goal;
import com.officelife.goals.Outcome;
import com.officelife.goals.State;
import com.officelife.goals.effects.Alternatives;
import com.officelife.goals.effects.Effect;
import com.officelife.goals.effects.TerminalAction;
import com.officelife.items.Weapon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class TryToAttackSomeone extends Goal {


    private boolean failed = false;

    private Actor target;

    private Effect e = null;


    public TryToAttackSomeone() {
    }

    @Override
    public Outcome outcome() {
        if (failed) {
            return Outcome.FAILURE;
        }
        return Outcome.CONTINUE;
    }

    @Override
    public Effect effect(State state) {
        Optional<Actor> maybe = chooseTarget(
                state,
                state.world.actorLocation(state.actor)
                        .orElseThrow(() -> new RuntimeException("Can't find actor"))
        );

        if (!maybe.isPresent()) {
            e = new TerminalAction(new Languish(state));
            return e;
        }
        this.target = maybe.get();
        e = new Alternatives(new Attack(this.target));
        return e;
    }

    private Optional<Actor> chooseTarget(State state, Coords personCoords) {
        List<Actor> nearby = new ArrayList<>();
        for (int i = personCoords.x - 5; i < personCoords.x + 5; i++) {
            for (int j = personCoords.y - 5; j < personCoords.y + 5; j++) {
                Coords coords = new Coords(i, j);
                if (state.world.actorLocations.containsKey(new Coords(i, j))) {
                    nearby.add(state.world.actorLocations.get(coords));
                }
            }
        }
        if (nearby.isEmpty()) {
            return Optional.empty();
        }

        List<Actor> withoutWeapons = nearby.stream()
                .filter(actor -> actor.inventory().stream().noneMatch(item -> item instanceof Weapon))
                .collect(Collectors.toList());

        Person actingPerson = (Person) state.actor;

        List<Actor> candidates;
        if (ThreadLocalRandom.current().nextInt(100) > 50) {
            candidates = withoutWeapons;
        } else {
            candidates = nearby;
        }

        // select actor with the lowest relationship score
        return candidates.stream()
                .filter(actor -> !actor.id().equals(state.actor.id()))
                .filter(actor -> actor instanceof Person)
                .min((actor1, actor2) -> {
                    int relationship1 = actingPerson.relationships.containsKey(actor1.id())
                            ? actingPerson.relationships.get(actor1.id())
                            : 0;
                    int relationship2 = actingPerson.relationships.containsKey(actor2.id())
                            ? actingPerson.relationships.get(actor2.id())
                            : 0;
                    return Integer.compare(relationship1, relationship2);
                });
    }

}
