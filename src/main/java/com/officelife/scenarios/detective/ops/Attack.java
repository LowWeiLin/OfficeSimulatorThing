package com.officelife.scenarios.detective.ops;

import static com.officelife.core.Action.State.CONTINUE;
import static com.officelife.core.planning.Facts.fact;
import static com.officelife.core.planning.Facts.facts;
import static com.officelife.core.planning.Facts.v;
import static com.officelife.scenarios.detective.Symbols.actor;
import static com.officelife.scenarios.detective.Symbols.has;
import static com.officelife.scenarios.detective.Symbols.hates;
import static com.officelife.scenarios.detective.Symbols.is;
import static com.officelife.scenarios.detective.Symbols.weapon;
import static com.officelife.utility.Utility.fit;

import com.officelife.core.Action;
import com.officelife.core.planning.Facts;
import com.officelife.core.planning.Node;
import com.officelife.core.planning.Op;

import javaslang.collection.Map;

public class Attack implements Op<Node> {

  private final String w = v();
  private final String target = v();

  @Override
  public Facts preconditions() {
    return facts(
      fact(w, is, weapon),
      fact(actor, has, w),
      fact(actor, hates, target));
  }

  @Override
  public int weight(Node state) {
    return 0;
  }

  @Override
  public Facts postconditions(Map<String, Object> bindings) {
    String w = (String) bindings.get(this.w).getOrElseThrow(fit);
    return facts(
      fact(w, is, weapon),
      fact(actor, has, w));
  }

  @Override
  public Action action() {
    return state -> CONTINUE;
  }
}
