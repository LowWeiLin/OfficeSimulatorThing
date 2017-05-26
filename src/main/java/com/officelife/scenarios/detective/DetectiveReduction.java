package com.officelife.scenarios.detective;

import static com.officelife.core.planning.Facts.fact;
import static com.officelife.core.planning.Facts.facts;
import static com.officelife.scenarios.detective.Symbols.actor;
import static com.officelife.scenarios.detective.Symbols.crowbar;
import static com.officelife.scenarios.detective.Symbols.edible;
import static com.officelife.scenarios.detective.Symbols.ground;
import static com.officelife.scenarios.detective.Symbols.apple;
import static com.officelife.scenarios.detective.Symbols.has;
import static com.officelife.scenarios.detective.Symbols.hates;
import static com.officelife.scenarios.detective.Symbols.is;
import static com.officelife.scenarios.detective.Symbols.weapon;

import com.officelife.core.WorldState;
import com.officelife.core.planning.Facts;
import com.officelife.core.planning.Reduction;

public class DetectiveReduction implements Reduction {

  @Override
  public Facts reduce(WorldState state) {

    return facts(
      fact(ground, has, crowbar),
      fact(crowbar, is, weapon),
      fact(ground, has, apple),
      fact(apple, is, edible),
      fact(actor, hates, "alice"));
  }
}
