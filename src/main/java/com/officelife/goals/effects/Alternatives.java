package com.officelife.goals.effects;

import static com.officelife.Utility.deque;

import java.util.Deque;

import com.officelife.goals.Goal;

public class Alternatives implements Effect {

  public final Deque<Goal> alternatives;

  public Alternatives(Goal... alternatives) {
    this(deque(alternatives));
  }

  public Alternatives(Deque<Goal> alternatives) {
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

