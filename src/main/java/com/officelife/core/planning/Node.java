package com.officelife.core.planning;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import astar.ASearchNode;
import astar.ISearchNode;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.Map;

/**
 * A node in the search space.
 *
 * Contains facts and knows about ways to transition to neighbouring nodes.
 */
public class Node extends ASearchNode {

  public final Facts facts;

  // The cost of getting here from the predecessor of this node.
  // TODO is there a less awkward way to store this?
  final int costFromPred;

  public final Op<Node> opFromPred;

  final List<Op<Node>> possibleActions;

  Search searchContext;

  public Node(Search searchContext, int costFromPred, Op<Node> opFromPred, Facts facts, List<Op<Node>>
    possibleActions) {
    this.searchContext = searchContext;
    this.facts = facts;
    this.costFromPred = costFromPred;
    this.opFromPred = opFromPred;
    this.possibleActions = possibleActions;
  }

  @Override
  public double h() {
    // The heuristic is the percentage of the goal contained in the current state.
    // TODO take into account the facts in the current state that do not contribute to the goal?

//    Set<Fact> remaining = new HashSet<>(searchContext.goalState());
//    int original = remaining.size();
//    remaining.removeAll(this.facts);
//    int contained = original - remaining.size();
//    double intersection = (double) contained / (double) searchContext.goalState().size();
//
//    // Higher percentage => lower cost
//
//    double n = 0;
//    double cost = (1 - intersection) * n;
//
//    // No idea what value of n relates % overlap and actual cost so that h is admissible,
//    // given that actual cost is arbitrarily decided.
//    // n = 0 is definitely correct, n = 1 is probably correct
//    return cost;

    return 0;
  }

  @Override
  public List<ISearchNode> getSuccessors() {

    // TODO intermediate collections are just for debugging
    List<Tuple2<Op<Node>, List<Map<String, Object>>>> candidates = possibleActions.stream()
      .map(o -> Tuple.of(o, facts.execute(o.preconditions())))
      .collect(Collectors.toList());

    List<ISearchNode> result = candidates.stream()
      .filter(o -> !o._2.isEmpty())
      .flatMap(o ->
        o._2.stream().map(solution ->
          new Node(
            searchContext,
            o._1.weight(this),
            o._1,
            facts.transitionWith(o._1, solution),
            possibleActions)))
      .collect(Collectors.toList());

    return result;
  }

  @Override
  public double c(ISearchNode successor) {
    return cast(successor).costFromPred + -1;// * stateScore.make(worldState).score();
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
  public Op<Node> op() {
    return opFromPred;
  }

  @Override
  public String toString() {
    return String.format("[%s]", facts.toString());
  }

  private static boolean meetsPreconditions(Facts toBeMet, Facts known) {
    return toBeMet.matches(known);
  }

  public static Node cast(ISearchNode other) {
    if (other instanceof Node) {
      return (Node) other;
    }
    throw new RuntimeException(other + " should not be in this set of nodes");
  }

}
