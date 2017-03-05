package com.officelife.actions;


import com.officelife.actions.prerequisite.LocationBeside;
import com.officelife.actors.Actor;
import com.officelife.actors.Person;
import com.officelife.goals.State;

public class Attack extends Action {

  private final Actor target;

  public Attack(State state, Actor target) {
    super(state);
    this.target = target;
  }

  @Override
  public boolean accept() {
    LocationBeside prereq = new LocationBeside(target, (Actor) state.person, state.world);
    if (!prereq.satisfied())  {
      System.out.println("Do Damage failing due to incorrect location");
      return false;
    }

    // take effect
    decreaseTargetHealth((Person) target);
    decreaseRelationshipValue(state.person, (Person) target);
    return true;
  }

  private void decreaseTargetHealth(Person target) {
    target.energy -= 5;
  }

  private void decreaseRelationshipValue(Person person, Person target) {
    String targetId = target.id();
    if (!person.relationships.containsKey(targetId)) {
      person.relationships.put(targetId, 0);
    }
    int currentRelationshipValue = person.relationships.get(targetId);
    person.relationships.put(targetId, currentRelationshipValue - 15);
    System.out.println("decrease relation!");
  }

  @Override
  public String toString() {
    return String.format("Attack Target %s", target.id());
  }
}
