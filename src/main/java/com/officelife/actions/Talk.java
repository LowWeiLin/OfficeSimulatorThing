package com.officelife.actions;

import com.officelife.Coords;
import com.officelife.actions.prerequisite.LocationBeside;
import com.officelife.actors.Actor;
import com.officelife.actors.Person;
import com.officelife.goals.State;

public class Talk extends Action {

  private final Actor target;

  public Talk(State state, Actor target) {
    super(state);
    this.target = target;
  }

  @Override
  public boolean accept() {
    LocationBeside prereq = new LocationBeside(target, (Actor) state.person, state.world);
    if (!prereq.satisfied())  {
      System.out.println("Talk failing due to incorrect location");
      return false;
    }

    // take effect
    Person person = state.person;
    increaseRelationshipValue(person, (Person) target);
    increaseRelationshipValue((Person) target, person);

    return true;
  }

  private void increaseRelationshipValue(Person person, Person target) {
    String targetId = target.id();
    if (!person.relationships.containsKey(targetId)) {
      person.relationships.put(targetId, 0);
    }
    int currentRelationshipValue = person.relationships.get(targetId);
    person.relationships.put(targetId, currentRelationshipValue + 5);
    System.out.println("Increment relation!");
  }

  @Override
  public String toString() {
    return String.format("Talk to Person %s", target.id());
  }
}