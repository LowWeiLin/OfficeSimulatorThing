
package com.officelife.utility;

import com.officelife.core.planning.Fact;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

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
}
