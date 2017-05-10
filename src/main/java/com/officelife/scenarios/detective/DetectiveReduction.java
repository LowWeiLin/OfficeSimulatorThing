package com.officelife.scenarios.detective;

import static com.officelife.core.planning.Facts.fact;
import static com.officelife.core.planning.Facts.facts;

import com.officelife.core.WorldState;
import com.officelife.core.planning.Facts;
import com.officelife.core.planning.Reduction;

public class DetectiveReduction implements Reduction {

  @Override
  public Facts reduce(WorldState state) {

    return facts(
      fact("ground", "has", "crowbar"),
      fact("crowbar", "is", "weapon"),
      fact("ground", "has", "apple"),
      fact("apple", "is", "edible"),
      fact("actor", "hates", "alice"));
  }
}
