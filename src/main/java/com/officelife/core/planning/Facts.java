package com.officelife.core.planning;

import static com.officelife.utility.Utility.set;
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.google.common.annotations.VisibleForTesting;
import com.officelife.utility.Utility;

import javaslang.collection.HashMap;
import javaslang.collection.Map;

/**
 * A database of facts.
 * <p>
 * Thread-safe if instances are not shared across threads.
 */
public class Facts {

  private static final int FIELDS = 3;

  private final Set<Fact> facts;

  public Facts(Set<Fact> facts) {
    this.facts = facts;
  }

  private static AtomicInteger var = new AtomicInteger(0);

  public static String v() {
    return "?" + var.getAndIncrement();
  }

  private static boolean isVar(Object v) {
    return v instanceof String && ((String) v).startsWith("?");
  }

  @VisibleForTesting
  public static void resetVar() {
    var.set(0);
  }

  public boolean isSubsetOf(Facts of) {
    return Utility.isSubsetOf(facts, of.facts);
  }

  private List<Map<String, Object>> answers;

  public List<Map<String, Object>> execute(Facts of) {
    answers = new ArrayList<>();
    unify(new ArrayList<>(of.facts), 0, HashMap.empty());
    return answers;
  }

  /**
   * Takes the given set of facts as a query and checks if the database can satisfy it.
   * Strictly more general than {@link #isSubsetOf(Facts)}.
   */
  public boolean matches(Facts of) {
    return !execute(of).isEmpty();
  }

  /**
   * Unifies a query (given as a list of non-ground facts) with the current database.
   * Uses a very simple structure for bindings because there are only variables on one side.
   * <p>
   * Tree height = O(query size)
   * Branching factor = O(size of DB subset that unifies)
   * <p>
   * This returns all solutions. To avoid explosions, give very specific queries which minimise
   * the branching factor.
   */
  private void unify(List<Fact> query, int fact, Map<String, Object> bindings) {

    if (fact == query.size()) {
      answers.add(bindings);
      return;
    }

    Fact qf = query.get(fact);
    for (Fact dbf : facts) {

      // Unify this fact and produce an updated list of bindings
      Map<String, Object> newBindings = bindings;

      prune:
      {
        for (int i = 0; i < FIELDS; i++) {
          if (isVar(qf.fields[i])) {
            // This cast is okay because only strings are vars
            String variable = (String) qf.fields[i];
            if (newBindings.containsKey(variable)) {
              if (!newBindings.get(variable).get().equals(dbf.fields[i])) {
                break prune;
              }
              // otherwise do nothing, i.e. unification succeeded
            } else {
              // Instantiate a variable and succeed
              newBindings = newBindings.put(variable, dbf.fields[i]);
            }
          } else {
            // Not a variable
            if (!qf.fields[i].equals(dbf.fields[i])) {
              break prune;
            }
            // otherwise do nothing, i.e. unification succeeded
          }
        }
        unify(query, fact + 1, newBindings);
      }
    }
  }

  Facts transitionWith(Op<?> op, Map<String, Object> bindings) {
    Set<Fact> copy = facts.stream()
      .map(f -> f.instantiate(bindings))
      .collect(Collectors.toSet());

    // TODO use a persistent set
    copy.removeAll(op.preconditions().facts);
    copy.addAll(op.postconditions(bindings).facts);

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
