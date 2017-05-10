package com.officelife.scenarios.detective;

import static com.officelife.core.planning.Node.cast;
import static com.officelife.utility.Utility.list;
import static java.util.stream.Collectors.toList;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import com.officelife.core.Action;
import com.officelife.core.Director;
import com.officelife.core.FirstWorld;
import com.officelife.core.WorldState;
import com.officelife.core.planning.Facts;
import com.officelife.core.planning.Node;
import com.officelife.core.planning.Op;
import com.officelife.core.planning.Search;
import com.officelife.scenarios.Person;
import com.officelife.scenarios.detective.ops.Attack;
import com.officelife.scenarios.detective.ops.Eat;
import com.officelife.scenarios.detective.ops.Take;

import astar.AStar;
import astar.ISearchNode;

public class DetectiveSearch implements Search {

  @Override
  public List<Op<Node>> operations() {
    return list(new Eat(), new Attack(), new Take());
  }

  @Override
  public Deque<Action> determineActions(WorldState state, Facts goal) {
    Facts facts = state.toFacts(new DetectiveReduction());

    List<ISearchNode> path = new AStar()
      .shortestPath(
        new Node(this,
          0,
          null,
          facts, operations()),

        // we're at the goal if the goal is completely contained in this node
        node -> cast(node).facts.matches(goal));

    if (path == null) {
      return new ArrayDeque<>();
    }

    System.out.println("path = " + path);

    return new ArrayDeque<>(path.stream()
      .map(ISearchNode::op)
      .filter(Objects::nonNull)
      .map(Op::action)
      .collect(toList()));
  }

  public static void main(String[] args) {
    Director director = new DetectiveDirector();
    Person person = new Person(director, "bob");
    Deque<Action> actions = new DetectiveSearch().determineActions(new WorldState(new FirstWorld(), person), director.getGoal(person));
    System.out.println("actions = " + actions);
  }
}
