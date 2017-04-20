package com.officelife.planning;

import java.util.Objects;

public class Fact {

  // TODO improve representation of knowledge
  public final String name;

  public final String subject;
  public final String object;
  public final String relation;
  public final int amount;

  public Fact(String name, String subject, String object, String relation, int amount) {
    this.name = name;
    this.subject = subject;
    this.object = object;
    this.relation = relation;
    this.amount = amount;
  }

  public Fact(String name) {
    this(name, "", "", "", 0);
  }

  public boolean satisfies(Fact other) {
    if (other.relation.isEmpty()) {
      return this.name.equals(other.name);
    }

    return Objects.equals(relation, other.relation)
            && Objects.equals(subject, other.subject)
            && Objects.equals(object, other.object)
            && amount <= other.amount;
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
