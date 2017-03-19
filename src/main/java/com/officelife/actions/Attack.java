package com.officelife.actions;


import com.officelife.items.Weapon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.officelife.actions.prerequisite.LocationBeside;
import com.officelife.actors.Actor;
import com.officelife.actors.Person;
import com.officelife.goals.State;

public class Attack extends Action {

  private static final Logger logger = LoggerFactory.getLogger(Attack.class);
  private final Actor target;

  public Attack(State state, Actor target) {
    super(state);
    this.target = target;
  }

  @Override
  public boolean accept() {
    LocationBeside prereq = new LocationBeside(target, state.actor, state.world);
    if (!prereq.satisfied())  {
      logger.warn("Attack failing due to incorrect location");
      return false;
    }

    if (!(state.actor instanceof Person)) {
      throw new RuntimeException("Attack requires person as actor");
    }

    Person person = (Person)state.actor;

    decreaseTargetHealth((Person) target);
    decreaseRelationshipValue(person, (Person) target);

    logger.debug("Attack completed");
    logger.debug("{} has {} energy remaining", target.id(), ((Person) target).energy);
    return true;
  }

  private void decreaseTargetHealth(Person target) {
    int baseDamage = 5;
    target.energy -= baseDamage + damageByWeapon();
  }

  private int damageByWeapon() {
    if (state.actor.inventory().stream()
            .noneMatch(item -> item instanceof Weapon)) {
      return 0;
    }
    Weapon mostPainful = state.actor.inventory()
            .stream()
            .filter(item -> item instanceof Weapon)
            .map(item -> (Weapon) item )
            .max((weapon1, weapon2) -> weapon1.damage() < weapon2.damage() ? -1 : 1)
            .get();
    return mostPainful.damage();
  }

  private void decreaseRelationshipValue(Person person, Person target) {
    String targetId = target.id();
    if (!person.relationships.containsKey(targetId)) {
      person.relationships.put(targetId, 0);
    }
    int currentRelationshipValue = person.relationships.get(targetId);
    person.relationships.put(targetId, currentRelationshipValue - 15);

  }

  @Override
  public String toString() {
    return String.format("Attack Target %s", target.id());
  }
}
