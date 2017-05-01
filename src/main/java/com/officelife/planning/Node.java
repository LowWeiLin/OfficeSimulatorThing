package com.officelife.planning;

import static com.officelife.Utility.isSubset;
import static com.officelife.planning.Planning.cast;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Triple;

import com.officelife.goals.State;
import com.officelife.planning.ops.wildling.WildlingStateScore;

import astar.ASearchNode;
import astar.ISearchNode;

public class Node extends ASearchNode {

  public final Set<Fact> facts;

  // The cost of getting here from the predecessor of this node.
  // TODO is there a less awkward way to store this?
  final int costFromPred;

  final List<Op<Node>> possibleActions;

  final State state;

  Planning planningContext;

  Op<Node> op;

  StateScore stateScore;

  public Node(
    Planning planningContext, int costFromPred, Set<Fact> facts, List<Op<Node>> possibleActions, State state,
    Op<Node> op, StateScore stateScore) {
    this.planningContext = planningContext;
    this.facts = facts;
    this.costFromPred = costFromPred;
    this.possibleActions = possibleActions;
    this.state = state;
    this.op = op;
    this.stateScore = stateScore;
  }

  @Override
  public double h() {
    // The heuristic is the percentage of the goal contained in the current state.
    // TODO take into account the facts in the current state that do not contribute to the goal?

    Set<Fact> remaining = new HashSet<>(planningContext.goalState());
    int original = remaining.size();
    remaining.removeAll(this.facts);
    int contained = original - remaining.size();
    double intersection = (double) contained / (double) planningContext.goalState().size();

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
      .map(o -> new Node(
        planningContext,
        o.weight(this),
        o.transition(facts),
        possibleActions,
        state,
        o,
        new WildlingStateScore()))
      .collect(Collectors.toList());
  }

  @Override
  public double c(ISearchNode successor) {
    return cast(successor).costFromPred + -1 * stateScore.make(state).score();
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

  @Override
  public Op<Node> op() {
    return op;
  }

  private static boolean meetsPreconditions(Set<Fact> toBeMet, Set<Fact> known) {
    return isSubset(toBeMet, known);
  }
}
