package com.officelife.utility;

import java.util.function.Predicate;

public class EndCoords implements Predicate<Coords> {

  private final Coords destination;

  public EndCoords(Coords destination) {
    this.destination = destination;
  }

  @Override
  public boolean test(Coords coords) {
    return coords.equals(destination);
  }
}

