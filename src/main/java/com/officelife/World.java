package com.officelife;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.officelife.actors.Actor;
import com.officelife.items.Item;

/**
 * Global state to mutate.
 */
public class World {


  public Map<Coords, Actor> actorLocations = new HashMap<>();
  private Map<Coords, List<Item>> itemLocations = new HashMap<>();

  public Optional<Coords> actorLocation(Actor actor) {
    return actorLocations.entrySet().stream()
      .filter(entry -> entry.getValue().equals(actor))
      .map(Map.Entry::getKey)
      .findFirst();
  }

  public Optional<Coords> itemLocation(Item item) {
    return itemLocations.entrySet().stream()
      .filter(entry -> entry.getValue().contains(item))
      .map(Map.Entry::getKey)
      .findFirst();
  }

  public List<Item> itemsAtLocation(Coords coords) {
    itemLocations.putIfAbsent(coords, new ArrayList<>());
    return itemLocations.get(coords);
  }

  public Optional<Coords> itemLocation(Predicate<Item> predicate) {
    return itemLocations.entrySet().stream()
      .filter(entry -> entry.getValue()
        .stream()
        .anyMatch(predicate::test)
      )
      .map(Map.Entry::getKey)
      .findFirst();
  }

  public Collection<Actor> actors() {
    return this.actorLocations.values();
  }

  public Collection<Item> items() {
    return this.itemLocations.values()
            .stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
  }

  public void removeActor(Actor actor) {
    this.actorLocations.values().remove(actor);
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

  public Optional<List<Coords>> findPath(Coords start, Predicate<Coords> target) {
    return findPath(start, target, 20);
  }

  public Optional<List<Coords>> findPath(Coords start, Predicate<Coords> target, int maxSteps) {
    Deque<Coords> q = new ArrayDeque<>();

    Set<Coords> visited = new HashSet<>();
    Map<Coords, Coords> parents = new HashMap<>();
    Map<Coords, Integer> numSteps = new HashMap<>();

    q.push(start);
    visited.add(start);
    numSteps.put(start, 0);

    Coords end = null;

    while (!q.isEmpty()) {
      Coords current = q.removeLast();
      int stepsTaken = numSteps.get(current);
      if (stepsTaken > maxSteps) {
        continue;
      }

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
          numSteps.put(n, stepsTaken + 1);
        }
      }
    }

    if (end == null) {
      return Optional.empty();
    }

    // rebuild the path

    Deque<Coords> path = new ArrayDeque<>();
    while (!end.equals(start)) {
      path.push(end);
      end = parents.get(end);
    }
    // the path will be without the start coord
    // path.push(start);

    return Optional.of(new ArrayList<>(path));
  }
}
