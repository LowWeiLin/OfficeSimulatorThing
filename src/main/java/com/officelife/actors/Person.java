package com.officelife.actors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import astar.AStar;
import astar.IGoalNode;
import astar.ISearchNode;
import astar.OpNode;
import com.officelife.Main;
import com.officelife.World;
import com.officelife.actions.Action;
import com.officelife.goals.Goal;
import com.officelife.goals.Goals;
import com.officelife.goals.State;
import com.officelife.items.Item;
import com.officelife.planning.Op;
import com.officelife.planning.Planning;
import com.officelife.planning.ops.wildling.WildlingPlanning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.officelife.Utility.isSubset;

public class Person implements Actor {

  private static final Logger logger = LoggerFactory.getLogger(Person.class);

  public int physiology = 10;
  public int belonging = 10;

  public int energy = 100;


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
    WildlingPlanning plan = new WildlingPlanning();
    IGoalNode goalCondition = node -> {
      // TODO world reduction
      return isSubset(plan.goalState(), Planning.cast(node).facts);
    };

    List<ISearchNode> path = new AStar()
            .shortestPath(
                    new Planning.Node(plan,
                            0,
                            plan.initialState(), plan.possibleActions(), null,null ),
                    goalCondition);

    if (path.size() < 2) {
      throw new RuntimeException("Does shortestPath return the starting node? Does it return a No-op?");
    }
//    return g.plan(new State(world, this), succeeded);
    ISearchNode chosen = path.get(1);
    Op<Planning.Node> op = chosen.op();

    logger.info("[op]" + op);

    Goals g = new Goals(op.goal());
    return g.plan(new State(world, this), succeeded);
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
