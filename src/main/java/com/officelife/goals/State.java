package com.officelife.goals;

import com.officelife.World;
import com.officelife.actors.Person;

public class State {
  public final World world;
  public final Person person;

  public State(World world, Person person) {
    this.world = world;
    this.person = person;
  }
}
