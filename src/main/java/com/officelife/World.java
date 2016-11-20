package com.officelife;

import com.officelife.actors.Actor;
import com.officelife.items.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * Global state to mutate.
 */
public class World {

    public Map<String, Actor> actors = new HashMap<>();
    public Map<String, Item> items = new HashMap<>();
    public Map<Pair<Integer, Integer>, String> locationActor = new HashMap<>();
    public Map<Pair<Integer, Integer>, String> locationItems = new HashMap<>();

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
                .orElseThrow(() -> new Exception("Cannot find actor"))
                .getKey();
    }
}
