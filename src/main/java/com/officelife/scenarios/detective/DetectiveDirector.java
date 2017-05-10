package com.officelife.scenarios.detective;

import static com.officelife.core.planning.Facts.fact;
import static com.officelife.core.planning.Facts.facts;

import com.officelife.core.Actor;
import com.officelife.core.Director;
import com.officelife.core.planning.Facts;

public class DetectiveDirector implements Director {
  @Override
  public Facts getGoal(Actor actor) {
    return facts(fact("actor", "feels", "full"));
  }
}
