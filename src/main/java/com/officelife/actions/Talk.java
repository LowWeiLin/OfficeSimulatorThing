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

    Person person = state.person;
    increaseRelationshipValue(person, (Person) target);
    increaseRelationshipValue((Person) target, person);
    increaseBelonging(person, (Person) target);

    System.out.println("Talk completed");
    return true;
  }

  private void increaseRelationshipValue(Person person, Person target) {
    String targetId = target.id();
    if (!person.relationships.containsKey(targetId)) {
      person.relationships.put(targetId, 0);
    }
    int currentRelationshipValue = person.relationships.get(targetId);
    person.relationships.put(targetId, currentRelationshipValue + 5);
  }

  private void increaseBelonging(Person person, Person target) {
    person.belonging += 5;
    person.belonging += 10;
  }

  @Override
  public String toString() {
    return String.format("Talk to Person %s", target.id());
  }
}
