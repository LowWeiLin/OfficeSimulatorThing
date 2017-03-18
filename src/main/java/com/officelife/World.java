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

  public Optional<Coords> closestLocation(Item item) {
    return itemLocations.entrySet().stream()
      .filter(entry -> entry.getValue().contains(item))
      .map(Map.Entry::getKey)
      .findFirst();
  }

  public List<Item> itemsAtLocation(Coords coords) {
    itemLocations.putIfAbsent(coords, new ArrayList<>());
    return itemLocations.get(coords);
  }


  public Optional<Coords> closestLocation(Predicate<Item> predicate, Coords current) {
    return itemLocations.entrySet().stream()
      .filter(entry -> entry.getValue()
        .stream()
        .anyMatch(predicate::test)
      )
      .map(Map.Entry::getKey)
            .sorted((coords1, coords2) -> distance(coords1, current) < distance(coords2, current) ? -1 : 1)
      .findFirst();
  }

  //
  public static long distance(Coords one, Coords two) {
    return Math.round(
            Math.sqrt(
                    Math.pow(one.x - two.x, 2)
                    + Math.pow(one.y - two.y, 2)
            )
    );
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
        if (!visited.contains(n) && !isObstructed(n)) {
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

  private boolean isObstructed(Coords n) {
    // actors block the way
    return Optional.ofNullable(this.actorLocations.get(n)).isPresent();
  }
}
