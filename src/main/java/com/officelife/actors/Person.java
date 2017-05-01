package com.officelife.actors;

import static com.officelife.Utility.isSubset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.officelife.World;
import com.officelife.actions.Action;
import com.officelife.goals.Goals;
import com.officelife.goals.State;
import com.officelife.items.Item;
import com.officelife.planning.Node;
import com.officelife.planning.Op;
import com.officelife.planning.Planning;
import com.officelife.planning.ops.wildling.WildlingPlanning;
import com.officelife.planning.ops.wildling.WildlingStateScore;

import astar.AStar;
import astar.IGoalNode;
import astar.ISearchNode;

public class Person implements Actor {

  private static final Logger logger = LoggerFactory.getLogger(Person.class);

  public int physiology = 10;
  public int belonging = 10;

  public int energy = 100;

  public Goals g;

  private final String id;

  public Person(String id, int physiology, int belonging, int energy) {
    this.id = id;
    this.physiology = physiology;
    this.belonging = belonging;
    this.energy = energy;
  }

  public Person(String id) {
    this(id, 10, 10, 10);
  }

  public final List<Item> inventory = new ArrayList<>();

  public final Map<String, Integer> relationships = new HashMap<>();

  @Override
  public String id() {
    return id;
  }

  @Override
  public Action act(World world, boolean succeeded) {
    if (g != null && g.hasGoals()) {
      return g.plan(new State(world, this), succeeded);
    } else {
      logger.debug(id + " has a new Op");
      WildlingPlanning plan = new WildlingPlanning();
      IGoalNode goalCondition = node -> {
        // TODO world reduction
        return isSubset(plan.goalState(), Planning.cast(node).facts);
      };

      State state = new State(world, this);
      List<ISearchNode> path = new AStar()
              .shortestPath(
                      new Node(plan,
                              0,
                              new WildlingStateScore(state).facts(),
                              plan.possibleActions(), state, null,
                              new WildlingStateScore()
                      ),
                      goalCondition);

      if (path.size() < 2) {
        throw new RuntimeException("Does shortestPath return the starting node? Does it return a No-op?");
      }

      ISearchNode chosen = path.get(1);
      Op<Node> op = chosen.op();

      logger.info("[op]" + op);

      g = new Goals(op.goal());
      return g.plan(new State(world, this), succeeded);
    }
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
