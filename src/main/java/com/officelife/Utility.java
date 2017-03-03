
package com.officelife;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utility {
  public static <T> List<T> list(T... objects) {
    return new ArrayList<>(Arrays.asList(objects));
  }
}
