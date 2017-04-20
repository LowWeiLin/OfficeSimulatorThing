package astar;
import com.officelife.planning.Op;

import java.util.*;

/**
 * Interface of a search node.
 */
public interface ISearchNode<A> {
    // total estimated cost of the node
    double f();
    //"tentative" g, cost from the start node 
    double g();
    //set "tentative" g
    void setG(double g);
    //heuristic cost to the goal node
    double h();
    //costs to a successor
    double c(ISearchNode successor);
    // a node possesses or computes his successors
    List<ISearchNode> getSuccessors();
    // get parent of node in a path
    ISearchNode getParent();
    //set parent
    void setParent(ISearchNode parent);
    //unique hash for a node to be used in a hash map
    //makes algorithm significantly faster
    //return null if you do not want this feature
    Integer keyCode();

    Op<A> op();

    boolean equals(Object other);

    int hashCode();
}

