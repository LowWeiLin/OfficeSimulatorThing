package com.officelife;

import java.util.HashMap;
import java.util.Map;

/**
 * Global state to mutate.
 */
public class World {

    Map<String, Actor> actors = new HashMap<>();
    Map<String, Item> items = new HashMap<>();
    Map<Pair<Integer, Integer>, String> locationActor = new HashMap<>();
    Map<Pair<Integer, Integer>, String> locationItems = new HashMap<>();

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

}
