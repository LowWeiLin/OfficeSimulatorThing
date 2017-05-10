package com.officelife;

import static com.officelife.core.planning.Facts.fact;
import static com.officelife.core.planning.Facts.facts;
import static com.officelife.core.planning.Facts.v;
import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.officelife.core.planning.Facts;

import javaslang.collection.HashMap;

public class FactsTest {

  @Before
  public void before() {
    Facts.resetVar();
  }

  @Test
  public void basics() {
    Facts db = facts(
      fact("x", "b", "c"),
      fact("y", "b", "c"));
    Facts query = facts(
      fact(v(), "b", "c"));
    assertEquals(Optional.of(HashMap.of("?0", "x")),
      db.execute(query));
  }

  @Test
  public void outOfOrder() {
    Facts db = facts(
      fact("x", "b", "y"),
      fact("y", "b", "c"));
    String a = v();
    String b = v();
    String c = v();
    Facts query = facts(
      fact(a, "b", b),
      fact(b, c, "c"),
      fact(a, c, "y"));
    assertEquals(Optional.of(HashMap.of("?0", "x", "?1", "y", "?2", "b")),
      db.execute(query));
  }

  @Test
  public void failure() {
    Facts db = facts(
      fact("x", "b", "y"),
      fact("y", "b", "c"));
    String a = v();
    String b = v();
    String c = v();
    Facts query = facts(
      fact(a, "b", b),
      fact(b, c, "c"),
      fact(a, c, "c")); // only difference from the above is y -> c
    assertEquals(Optional.empty(), db.execute(query));
  }
}
