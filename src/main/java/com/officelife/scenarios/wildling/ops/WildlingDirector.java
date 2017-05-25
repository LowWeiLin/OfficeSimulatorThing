package com.officelife.scenarios.wildling.ops;


import com.officelife.core.Actor;
import com.officelife.core.Director;
import com.officelife.core.planning.Facts;

import static com.officelife.core.planning.Facts.fact;
import static com.officelife.core.planning.Facts.facts;

import static com.officelife.core.planning.Facts.v;

public class WildlingDirector implements Director {
    @Override
    public Facts getGoal(Actor actor) {
        String a = v();
        return facts(
                fact("actor", "is", a),
                fact(a, "is south of", "The Wall")
        );
    }

}
