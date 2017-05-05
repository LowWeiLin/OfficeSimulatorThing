package com.officelife.scenarios;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.officelife.core.Actor;
import com.officelife.core.Director;
import com.officelife.core.FirstWorld;
import com.officelife.core.Planning;
import com.officelife.core.WorldState;

import com.officelife.scenarios.items.Item;

public class Person implements Actor {

  private static final Logger logger = LoggerFactory.getLogger(Person.class);

  public int physiology = 10;
  public int belonging = 10;

  public int energy = 100;

  public final Planning planning = new Planning();

  private final String id;

  public final Director director;

  public Person(Director director, String id, int physiology, int belonging, int energy) {
    this.id = id;
    this.physiology = physiology;
    this.belonging = belonging;
    this.energy = energy;
    this.director = director;
  }

  public Person(Director director, String id) {
    this(director, id, 10, 10, 10);
  }

  public final List<Item> inventory = new ArrayList<>();

  public final Map<String, Integer> relationships = new HashMap<>();

  @Override
  public String id() {
    return id;
  }

  @Override
  public void act(FirstWorld world) {

    // TODO need some way to update the director of goal progress
    planning.act(new WorldState(world, this), director.getGoal(this));

//    if (g != null && g.hasGoals()) {
//      return g.plan(new WorldState(world, this), succeeded);
//    } else {
//      logger.debug(id + " has a new Op");
//      WildlingSearch plan = new WildlingSearch();
//      IGoalNode goalCondition = node -> {
//        // TODO world reduction
//        return isSubsetOf(plan.goalState(), Search.cast(node).facts);
//      };
//
//      WorldState worldState = new WorldState(world, this);
//      List<ISearchNode> path = new AStar()
//              .shortestPath(
//                      new Node(plan,
//                              0,
//                              new WildlingStateScore(worldState).facts(),
//                              plan.operations(), worldState, null,
//                              new WildlingStateScore()
//                      ),
//                      goalCondition);
//
//      if (path.size() < 2) {
//        throw new RuntimeException("Does shortestPath return the starting node? Does it return a No-op?");
//      }
//
//      ISearchNode chosen = path.get(1);
//      Op<Node> op = chosen.op();
//
//      logger.info("[op]" + op);
//
//      g = new Goals(op.goal());
//      return g.plan(new WorldState(world, this), succeeded);
//    }
  }

  @Override
  public List<Item> inventory() {
    return this.inventory;
  }

  @Override
  public void addItem(Item item) {
    inventory.add(item);
  }

  @Override
  public boolean isDead() {return this.energy <= 0;}

  @Override
  public char[][] textRepresentation() {
    return new char[][]{{'P'}};
  }
}
