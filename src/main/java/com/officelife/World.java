package com.officelife;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import com.officelife.actors.Actor;
import com.officelife.items.Item;

/**
 * Global state to mutate.
 */
public class World {

  public Map<String, Actor> actors = new HashMap<>();
  public Map<String, Item> items = new HashMap<>();
  public Map<Coords, String> actorLocations = new HashMap<>();
  public Map<Coords, String> itemLocations = new HashMap<>();

  public Optional<Coords> actorLocation(String actorId) {
    return actorLocations.entrySet().stream()
      .filter(entry -> entry.getValue().equals(actorId))
      .map(Map.Entry::getKey)
      .findFirst();
  }

  public Optional<Coords> itemLocation(String itemId) {
    return itemLocations.entrySet().stream()
      .filter(entry -> entry.getValue().equals(itemId))
      .map(Map.Entry::getKey)
      .findFirst();
  }

  public Optional<Coords> itemLocation(Predicate<Item> predicate) {
    return itemLocations.entrySet().stream()
      .filter(entry -> predicate.test(items.get(entry.getValue())))
      .map(Map.Entry::getKey)
      .findFirst();
  }

  public void removeActor(Actor actor) {
    this.actors.remove(actor.id());
    this.actorLocations.values().remove(actor.id());
  }

  public static class EndCoords implements Predicate<Coords> {

    private final Coords destination;

    public EndCoords(Coords destination) {
      this.destination = destination;
    }

    @Override
    public boolean test(Coords coords) {
      return coords.equals(destination);
    }
  }

  public List<Coords> findPath(Coords start, Predicate<Coords> target) {
    Deque<Coords> q = new ArrayDeque<>();

    Set<Coords> visited = new HashSet<>();
    Map<Coords, Coords> parents = new HashMap<>();

    q.push(start);
    visited.add(start);

    Coords end = null;

    while (!q.isEmpty()) {
      Coords current = q.removeLast();
      if (target.test(current)) {
        end = current;
        break;
      }
      Set<Coords> neighbours = current.neighbours();
      for (Coords n : neighbours) {
        if (!visited.contains(n)) {
          parents.put(n, current);
          q.push(n);
          visited.add(n);
        }
      }
    }

    if (end == null) {
      // TODO is this even possible? it will loop forever if the target is never found
      throw new RuntimeException("did not find target");
    }

    // rebuild the path

    Deque<Coords> path = new ArrayDeque<>();
    while (!end.equals(start)) {
      path.push(end);
      end = parents.get(end);
    }
    // the path will be without the start coord
    // path.push(start);

    return new ArrayList<>(path);
  }
}
