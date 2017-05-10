package astar.datastructures;

import java.util.List;

import astar.ISearchNode;

public interface IClosedSet {

	boolean contains(ISearchNode node);
	void add(ISearchNode node);
	ISearchNode min();

	List<ISearchNode> get();
}
