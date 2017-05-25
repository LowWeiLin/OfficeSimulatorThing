package com.officelife.scenarios.wildling.ops;

import com.officelife.core.WorldState;
import com.officelife.core.planning.Facts;
import com.officelife.core.planning.Reduction;

import static com.officelife.core.planning.Facts.fact;
import static com.officelife.core.planning.Facts.facts;

/**
 * Created by user on 21/5/2017.
 */
public class WildlingReduction implements Reduction {
    @Override
    public Facts reduce(WorldState state) {

        return facts(
                fact("Fruit", "can be", "eaten")
        );
    }
}
