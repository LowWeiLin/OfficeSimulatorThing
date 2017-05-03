package com.officelife.scenarios.wood;

import static com.officelife.core.planning.Facts.fact;
import static com.officelife.core.planning.Facts.facts;

import com.officelife.core.Actor;
import com.officelife.core.Director;
import com.officelife.core.planning.Fact;
import com.officelife.core.planning.Facts;

public class WoodcutterDirector implements Director {
  @Override
  public Facts getGoal(Actor actor) {
    return facts(fact("actor", "has", "axe"));
  }
}
