package com.officelife.scenarios.wood;


import static com.officelife.core.planning.Node.cast;
import static com.officelife.utility.Utility.list;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import com.officelife.core.Action;
import com.officelife.core.WorldState;
import com.officelife.core.planning.Facts;
import com.officelife.core.planning.Node;
import com.officelife.core.planning.Op;
import com.officelife.core.planning.Search;
import com.officelife.scenarios.wood.ops.GetAxe;

import astar.AStar;
import astar.IGoalNode;
import astar.ISearchNode;

public class WoodcutterSearch implements Search {

  @Override
  public List<Op<Node>> operations() {
//        return list(new ChopLog(), new GetAxe(), new CollectBranches());
    return list(new GetAxe());
  }

  public Deque<Action> determineActions(WorldState state, Facts goal) {
    Facts facts = state.toFacts(new WoodcutterReduction());

    // TODO there's no point to IGoalNodes; they're just predicates
    IGoalNode goalCondition = node -> {
      // we're at the goal if the goal is completely contained in this node
      return goal.isSubsetOf(cast(node).facts);
    };

    List<ISearchNode> path = new AStar()
      .shortestPath(
        new Node(this,
          0,
          null,
          facts, operations()),
        goalCondition);

    path.forEach(System.out::println);

    // TODO wut why does this crash
//        List<Action> ops = path.stream()
//          .filter(Objects::nonNull)
//          .map(ISearchNode::op)
//          .map(Op::action)
//          .collect(Collectors.toList());

    // TODO check list size
    List<Action> ops = Arrays.asList(path.get(1).op().action());

    return new ArrayDeque<>(ops);
  }
}
