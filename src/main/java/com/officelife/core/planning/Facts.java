package com.officelife.core.planning;

import static com.officelife.utility.Utility.set;
import static java.util.stream.Collectors.joining;

import java.util.HashSet;
import java.util.Set;

import com.officelife.utility.Utility;

/**
 * Encapsulation for a set of facts.
 *
 * TODO use a persistent set of some kind, e.g. vavr
 */
public class Facts {

  private final Set<Fact> facts;

  public Facts(Set<Fact> facts) {
    this.facts = facts;
  }

  public boolean isSubsetOf(Facts of) {
    return Utility.isSubsetOf(facts, of.facts);
  }

  public Facts transitionWith(Op<?> op) {
    Set<Fact> copy = new HashSet<>(facts);

    copy.removeAll(op.preconditions().facts);
    copy.addAll(op.postconditions().facts);

    return new Facts(copy);
  }

  @Override
  public String toString() {
    return facts.stream()
      .map(Object::toString)
      .collect(joining(", "));
  }

  public static Facts facts(Fact... facts) {
    return new Facts(set(facts));
  }

  public static Fact fact(String subject, String relation, Object object) {
    return new Fact(subject, relation, object);
  }
}
