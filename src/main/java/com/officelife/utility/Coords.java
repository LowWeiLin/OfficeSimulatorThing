package com.officelife.utility;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Coords {
  public final int x;
  public final int y;

  public Coords(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public Set<Coords> neighbours() {
    Set<Coords> result = new HashSet<>();
    result.add(new Coords(x, y + 1));
    result.add(new Coords(x, y - 1));
    result.add(new Coords(x + 1, y));
    result.add(new Coords(x - 1, y));
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Coords coords = (Coords) o;
    return x == coords.x &&
      y == coords.y;
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y);
  }

  @Override
  public String toString() {
    return String.format("(%d, %d)", x, y);
  }
}
