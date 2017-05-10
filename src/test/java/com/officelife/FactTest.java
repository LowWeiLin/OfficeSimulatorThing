package com.officelife;

import static com.officelife.core.planning.Facts.fact;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import javaslang.collection.HashMap;

public class FactTest {

  @Test
  public void basics() {
    assertEquals(
      fact("a", "b", "c"),
      fact("?0", "b", "?1")
        .instantiate(HashMap.of("?0", "a", "?1", "c")));
  }

  @Test
  public void randomObjects() {
    assertEquals(
      fact("a", "b", 1),
      fact("?0", "b", 1)
        .instantiate(HashMap.of("?0", "a")));
  }
}
