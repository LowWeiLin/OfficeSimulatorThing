package com.officelife.scenarios.wildling.ops;

import astar.AStar;
import astar.ISearchNode;
import com.google.common.base.Stopwatch;
import com.officelife.core.Action;
import com.officelife.core.Director;
import com.officelife.core.FirstWorld;
import com.officelife.core.WorldState;
import com.officelife.core.planning.Facts;
import com.officelife.core.planning.Node;
import com.officelife.core.planning.Op;
import com.officelife.core.planning.Search;
import com.officelife.scenarios.Person;
import com.officelife.scenarios.detective.DetectiveDirector;
import com.officelife.scenarios.detective.DetectiveReduction;
import com.officelife.scenarios.detective.ops.Attack;
import com.officelife.scenarios.detective.ops.Eat;
import com.officelife.scenarios.detective.ops.Take;
import javaslang.collection.HashMap;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.officelife.core.planning.Node.cast;
import static com.officelife.utility.Utility.list;
import static java.util.stream.Collectors.toList;

public class WildlingSearch implements Search {

  @Override
  public List<Op<Node>> operations() {
    return list(new Eat(), new Attack(), new Take());
  }

  @Override
  public Deque<Action> determineActions(WorldState state, Facts goal) {
    Facts facts = state.toFacts(new WildlingReduction());

    List<ISearchNode> path = new AStar()
      .shortestPath(
        new Node(this,
          0,
          null,
          HashMap.of(),
          facts, operations()),

        // we're at the goal if the goal is completely contained in this node
        node -> cast(node).facts.matches(goal));

    if (path == null) {
      System.out.println("not found");
      return new ArrayDeque<>();
    }

    System.out.println("path = " + path);

    return new ArrayDeque<>(path.stream()
      .map(ISearchNode::op)
      .filter(Objects::nonNull)
      .map(Op::action)
      .collect(toList()));
  }

}
