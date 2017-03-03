package com.officelife.goals.effects;

import static com.officelife.Utility.list;

import java.util.List;

import com.officelife.goals.Goal;

public class Alternatives implements Effect {

  public final List<Goal> alternatives;

  public Alternatives(Goal... alternatives) {
    this(list(alternatives));
  }

  public Alternatives(List<Goal> alternatives) {
    this.alternatives = alternatives;
  }

  @Override
  public boolean isTerminal() {
    return false;
  }

  @Override
  public boolean hasWorkLeft() {
    return !alternatives.isEmpty();
  }
}

