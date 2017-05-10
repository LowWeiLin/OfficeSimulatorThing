package com.officelife.scenarios.wood;

import static com.officelife.core.planning.Facts.fact;
import static com.officelife.core.planning.Facts.facts;

import java.util.Optional;

import com.officelife.core.WorldState;
import com.officelife.core.planning.Facts;
import com.officelife.core.planning.Reduction;
import com.officelife.scenarios.items.Pants;
import com.officelife.utility.Coords;

public class WoodcutterReduction implements Reduction {

  @Override
  public Facts reduce(WorldState state) {

    Optional<Coords> coords =
      state.world.items().stream().filter(i -> i instanceof Pants)
        .findFirst().flatMap(state.world::itemLocation);

    if (coords.isPresent()) {
      return facts(fact("ground", "has", "pants"));
    } else {
      return facts();
    }
  }
}
