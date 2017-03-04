
package com.officelife;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public class Utility {
  public static <T> List<T> list(T... objects) {
    return new ArrayList<>(Arrays.asList(objects));
  }

  public static <T> Deque<T> deque(T... objects) {
    return new ArrayDeque<T>(Arrays.asList(objects));
  }
}
