package com.officelife.scenarios.detective.ops;

import static com.officelife.core.Action.State.CONTINUE;
import static com.officelife.core.planning.Facts.fact;
import static com.officelife.core.planning.Facts.facts;
import static com.officelife.core.planning.Facts.v;
import static com.officelife.utility.Utility.fit;

import com.officelife.core.Action;
import com.officelife.core.planning.Facts;
import com.officelife.core.planning.Node;
import com.officelife.core.planning.Op;

import javaslang.collection.Map;

public class Attack implements Op<Node> {

  private final String weapon = v();
  private final String target = v();

  @Override
  public Facts preconditions() {
    return facts(
      fact(weapon, "is", "weapon"),
      fact("actor", "has", weapon),
      fact("actor", "hates", target));
  }

  @Override
  public int weight(Node state) {
    return 0;
  }

  @Override
  public Facts postconditions(Map<String, Object> bindings) {
    return facts(
      fact((String) bindings.get(weapon).getOrElseThrow(fit), "is", "weapon"),
      fact("actor", "has", bindings.get(weapon).getOrElseThrow(fit)),
      fact("actor", "hates", bindings.get(weapon).getOrElseThrow(fit)));
  }

  @Override
  public Action action() {
    return state -> CONTINUE;
  }
}
