package com.officelife.planning.ops.wildling;

import astar.AStar;
import astar.IGoalNode;
import astar.ISearchNode;
import com.officelife.planning.Fact;
import com.officelife.planning.Op;
import com.officelife.planning.Planning;

import java.util.List;
import java.util.Set;

import static com.officelife.Utility.isSubset;
import static com.officelife.Utility.list;
import static com.officelife.Utility.set;

/**
 * “Free folk don't follow names, or little cloth animals sewn on a tunic,"
 * the King-Beyond-the-Wall had told him. "They won't dance for coins,
 * they don't care how your style yourself or what that chain of office means or who your grandsire was.
 * They follow strength.
 * They follow the man.”
 */
public class WildlingPlanning extends Planning {

    @Override
    public Set<Fact> initialState() {
        return set(new Fact("I am starving", "me", null, "hunger", 50));
    }

    @Override
    public Set<Fact> goalState() {
        return set(
                new Fact("I feel safe now"));
    }

    @Override
    public List<Op<Node>> possibleActions() {
        return list(new FightForFood(), new HeadSouth());
    }

    public static void main(String[] args) {

        WildlingPlanning plan = new WildlingPlanning();
        IGoalNode goalCondition = node -> {
            // TODO world reduction
            return isSubset(plan.goalState(), cast(node).facts);
        };

        List<ISearchNode> path = new AStar()
                .shortestPath(
                        new Node(plan,
                                0,
                                plan.initialState(), plan.possibleActions(), null,null ),
                        goalCondition);

        path.forEach(System.out::println);
    }
}
