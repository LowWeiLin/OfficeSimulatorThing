package com.officelife.planning;

import java.util.Objects;

public class Fact {

  // TODO improve representation of knowledge
  public final String name;

  public Fact(String name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Fact fact = (Fact) o;
    return Objects.equals(name, fact.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public String toString() {
    return name;
  }
}
