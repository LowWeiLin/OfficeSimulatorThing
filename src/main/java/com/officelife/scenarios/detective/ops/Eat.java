package com.officelife.scenarios.detective.ops;

import static com.officelife.core.Action.State.CONTINUE;
import static com.officelife.core.planning.Facts.fact;
import static com.officelife.core.planning.Facts.facts;
import static com.officelife.core.planning.Facts.v;
import static com.officelife.scenarios.detective.Symbols.actor;
import static com.officelife.scenarios.detective.Symbols.edible;
import static com.officelife.scenarios.detective.Symbols.feels;
import static com.officelife.scenarios.detective.Symbols.full;
import static com.officelife.scenarios.detective.Symbols.has;
import static com.officelife.scenarios.detective.Symbols.is;

import com.officelife.core.Action;
import com.officelife.core.planning.Facts;
import com.officelife.core.planning.Node;
import com.officelife.core.planning.Op;

import javaslang.collection.Map;

public class Eat implements Op<Node> {

  private final String food = v();

  @Override
  public Facts preconditions() {
    return facts(
      fact(food, is, edible),
      fact(actor, has, food));
  }

  @Override
  public int weight(Node state) {
    return 0;
  }

  @Override
  public Facts postconditions(Map<String, Object> bindings) {
    return facts(
      fact(actor, feels, full));
  }

  @Override
  public Action action() {
    return state -> CONTINUE;
  }
}
