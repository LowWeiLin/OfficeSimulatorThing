package com.officelife.scenarios.wood.ops;

import static com.officelife.core.planning.Facts.facts;

import com.officelife.core.Action;
import com.officelife.core.planning.Fact;
import com.officelife.core.planning.Facts;
import com.officelife.core.planning.Node;
import com.officelife.core.planning.Op;

public class ChopLog implements Op<Node> {

  @Override
  public Facts preconditions() {
    return facts(new Fact("actor", "has", "axe"));
  }

  @Override
  public int weight(Node state) {
    return 4;
  }

  @Override
  public Facts postconditions() {
    return facts(new Fact("actor", "has", "firewood"));
  }

  @Override
  public Action action() {
    return state -> null;
  }
}