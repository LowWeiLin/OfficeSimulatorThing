package com.officelife.planning.ops.wood;


import astar.AStar;
import astar.IGoalNode;
import astar.ISearchNode;
import com.officelife.planning.Fact;
import com.officelife.planning.Op;
import com.officelife.planning.Planning;
import com.officelife.planning.ops.wildling.WildlingStateScore;

import java.util.List;
import java.util.Set;

import static com.officelife.Utility.isSubset;
import static com.officelife.Utility.list;
import static com.officelife.Utility.set;

public class WoodcutterPlanning extends Planning {


    @Override
    public Set<Fact> initialFacts() {
        return set(
                new Fact("an axe is available"),
                new Fact("i don't have axe"),
                new Fact("the sun is shining"));
    }

    @Override
    public Set<Fact> goalState() {
        return set(
                new Fact("i have firewood"));
    }

    @Override
    public List<Op<Node>> possibleActions() {
        return list(new ChopLog(), new GetAxe(), new CollectBranches());
    }

    public static void main(String[] args) {

        WoodcutterPlanning woodcuttingPlan = new WoodcutterPlanning();
        // TODO there's no point to IGoalNodes; they're just predicates
        IGoalNode goalCondition = node -> {
            // we're at the goal if the goal is completely contained in this node
            return isSubset(woodcuttingPlan.goalState(), cast(node).facts);
        };

        List<ISearchNode> path = new AStar()
                .shortestPath(
                    new Node(woodcuttingPlan,
                        0,
                        woodcuttingPlan.initialFacts(), woodcuttingPlan.possibleActions(), null,null,
                            new WildlingStateScore()),
                    goalCondition);

        path.forEach(System.out::println);
    }
}
