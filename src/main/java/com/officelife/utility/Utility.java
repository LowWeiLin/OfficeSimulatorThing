
package com.officelife.utility;

import static com.officelife.core.planning.Node.cast;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import astar.ISearchNode;
import astar.datastructures.IClosedSet;

public class Utility {
  public static <T> List<T> list(T... objects) {
    return new ArrayList<>(Arrays.asList(objects));
  }

  public static <T> Deque<T> deque(T... objects) {
    return new ArrayDeque<>(Arrays.asList(objects));
  }

  public static <T> Set<T> set(T... objects) {
    return new HashSet<>(Arrays.asList(objects));
  }

  /**
   * insideOf is the larger set
   */
  public static <T> boolean isSubsetOf(Set<T> inside, Set<T> insideOf) {
    Set<T> copy = new HashSet<>(inside);
    copy.removeAll(insideOf);
    return copy.isEmpty();
  }

  public static final Supplier<Error> fit = () ->
    new AssertionError("something dumb just happened");

  public static String renderClosedSet(IClosedSet closedSet) {

    List<ISearchNode> nodes = closedSet.get();

    StringBuilder sb = new StringBuilder();
    for (ISearchNode node : nodes) {
      for (Object o : node.getSuccessors()) {
        ISearchNode s = (ISearchNode) o;
        sb.append(node.keyCode());
        sb.append(" -> ");
        sb.append(s.keyCode());
        sb.append("[label=\"");
        sb.append(s.op().getClass().getSimpleName());
        sb.append(" ");
        sb.append(cast(s).bindings
          .toString().replaceAll("HashMap", ""));
        sb.append("\"]");
        sb.append(";");
      }
    }

    return "digraph G { rankdir=LR; " + sb.toString() + "}";
  }
}
