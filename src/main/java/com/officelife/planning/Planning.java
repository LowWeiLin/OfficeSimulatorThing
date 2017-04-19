package com.officelife.planning;

import static com.officelife.Utility.list;
import static com.officelife.Utility.set;

import java.util.*;
import java.util.stream.Collectors;

import com.officelife.goals.State;
import com.officelife.planning.ops.wood.ChopLog;
import com.officelife.planning.ops.wood.CollectBranches;
import com.officelife.planning.ops.wood.GetAxe;

import astar.ASearchNode;
import astar.AStar;
import astar.IGoalNode;
import astar.ISearchNode;

public abstract class Planning {

  private static Set<Fact> initialState = set(
    new Fact("an axe is available"),
    new Fact("i don't have axe"),
    new Fact("the sun is shining"));

  private static Set<Fact> goalState = set(
    new Fact("i have firewood"));

  private static List<Op<Node>> possibleActions() {
    return list(new ChopLog(), new GetAxe(), new CollectBranches());
  }

  private State state() {
    // TODO this may not be required after all? Not sure if we want to use information from here to plan
    return null;
  }

  private static Node cast(ISearchNode other) {
    if (other instanceof Node) {
      return (Node) other;
    }
    throw new RuntimeException(other + " should not be in this set of nodes");
  }

  private static boolean isSubset(Set<Fact> inside, Set<Fact> insideOf) {
    Set<Fact> copy = new HashSet<>(inside);
    copy.removeAll(insideOf);
    return copy.isEmpty();
  }

  private static boolean meetsPreconditions(Set<Fact> toBeMet, Set<Fact> known) {
    return isSubset(toBeMet, known);
  }

  public static class Node extends ASearchNode {

    final Set<Fact> facts;

    // The cost of getting here from the predecessor of this node.
    // TODO is there a less awkward way to store this?
    final int costFromPred;

    final List<Op<Node>> possibleActions;

    final State state;

    Node(int costFromPred, Set<Fact> facts, List<Op<Node>> possibleActions, State state) {
      this.facts = facts;
      this.costFromPred = costFromPred;
      this.possibleActions = possibleActions;
      this.state = state;
    }

    @Override
    public double h() {
      // The heuristic is the percentage of the goal contained in the current state.
      // TODO take into account the facts in the current state that do not contribute to the goal?

      Set<Fact> remaining = new HashSet<>(goalState);
      int original = remaining.size();
      remaining.removeAll(this.facts);
      int contained = original - remaining.size();
      double intersection = (double) contained / (double) goalState.size();

      // Higher percentage => lower cost

      double n = 0;
      double cost = (1 - intersection) * n;

      // No idea what value of n relates % overlap and actual cost so that h is admissible,
      // given that actual cost is arbitrarily decided.
      // n = 0 is definitely correct, n = 1 is probably correct
      return cost;
    }

    @Override
    public List<ISearchNode> getSuccessors() {
      List<Op<Node>> chosen = this.possibleActions.stream()
              .filter(o -> meetsPreconditions(o.preconditions(), facts))
              .collect(Collectors.toList());
//      return null;
//
      return chosen.stream()
              .map(o -> new AbstractMap.SimpleEntry<>(o.weight(this), o.transition(facts)))
              .map(e -> new Node(e.getKey(), e.getValue(), possibleActions, state))
              .collect(Collectors.toList());
    }

    @Override
    public double c(ISearchNode successor) {
      return cast(successor).costFromPred;
    }

    // More trivial stuff

    Node parent;

    @Override
    public ISearchNode getParent() {
      return parent;
    }

    @Override
    public void setParent(ISearchNode parent) {
      this.parent = cast(parent);
    }

    @Override
    public Integer keyCode() {
      return null;
    }

    @Override
    public String toString() {
      return String.format("[%s]", facts.stream()
        .map(Object::toString)
        .collect(Collectors.joining(", ")));
    }
  }

  public static void main(String[] args) {

    // TODO there's no point to IGoalNodes; they're just predicates
    IGoalNode goalCondition = node -> {
      // we're at the goal if the goal is completely contained in this node
      return isSubset(goalState, cast(node).facts);
    };

    ArrayList<ISearchNode> path = new AStar().shortestPath(
            new Node(0, initialState, possibleActions(), null ), goalCondition);

    path.forEach(System.out::println);
  }
}
