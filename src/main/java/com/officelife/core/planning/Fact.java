package com.officelife.core.planning;

import java.util.Objects;

/**
 * An RDF triple.
 */
public class Fact {

  public final String subject;
  public final String relation;
  public final Object object;

  public final Object[] fields;

  public Fact(String subject, String relation, Object object) {
    this.subject = subject;
    this.object = object;
    this.relation = relation;
    fields = new Object[] {subject, object, relation};
  }

  public int amount() {
    return (int) object;
  }

  public String value() {
    return (String) object;
  }

  public boolean satisfies(Fact other) {
    return Objects.equals(relation, other.relation)
            && Objects.equals(subject, other.subject)
            && Objects.equals(object, other.object)
            && amount() <= other.amount();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Fact fact = (Fact) o;
    return Objects.equals(subject, fact.subject) &&
      Objects.equals(relation, fact.relation) &&
      Objects.equals(object, fact.object);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, relation, object);
  }

  @Override
  public String toString() {
    return "Fact{" +
      "subject='" + subject + '\'' +
      ", relation='" + relation + '\'' +
      ", object=" + object +
      '}';
  }
}
