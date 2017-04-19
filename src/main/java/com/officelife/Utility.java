
package com.officelife;

import com.officelife.planning.Fact;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

  public static <T> boolean isSubset(Set<T> inside, Set<T> insideOf) {
    Set<T> copy = new HashSet<>(inside);
    copy.removeAll(insideOf);
    return copy.isEmpty();
  }
}
