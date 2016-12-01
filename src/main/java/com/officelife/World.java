package com.officelife;

import com.officelife.actors.Actor;
import com.officelife.common.Pair;
import com.officelife.items.Item;
import com.officelife.locations.Default;
import com.officelife.locations.LocationTrait;

import java.util.*;

/**
 * Global state to mutate.
 */
public class World {

    public Map<String, Actor> actors = new HashMap<>();
    public Map<String, Item> items = new HashMap<>();
    public Map<Pair<Integer, Integer>, String> locationActor = new HashMap<>();
    public Map<Pair<Integer, Integer>, String> locationItems = new HashMap<>();
    private Map<Pair<Integer, Integer>, LocationTrait> locationTraits = new HashMap<>();

    /**
     * @param actorId identifier of the actor
     * @return (x, y) giving x and y position of the actor
     * @throws Exception if can't find
     */
    public Pair<Integer, Integer> actorLocation(String actorId) throws Exception {
        return locationActor.entrySet().stream()
            .filter(entry -> entry.getValue().equals(actorId))
            .findFirst()
            .orElseThrow(() -> new Exception("Cannot find actor"))
            .getKey();
    }

    public Pair<Integer, Integer> itemLocation(String itemId) throws Exception {
        return locationItems.entrySet().stream()
                .filter(entry -> entry.getValue().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new Exception("Cannot find item"))
                .getKey();
    }

    public Optional<Item> itemWithClass(Class<? extends Item> itemClass) {
        return locationItems.entrySet().stream()
                .map(entry -> entry.getValue())
                .map(itemId -> this.items.get(itemId))
                .filter(item -> item.getClass() == itemClass)
                .findFirst();
    }

    public void addToLocation(Pair<Integer, Integer> location, LocationTrait trait) {

        locationTraits.put(location, trait);
    }

    public LocationTrait locationTrait(Pair<Integer, Integer> location) {
        return locationTraits.containsKey(location) ? locationTraits.get(location) : new Default();
    }
}
