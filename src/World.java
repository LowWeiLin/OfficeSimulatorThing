
import java.util.Map;

/**
 * Global
 */
public class World {


    Map<String, Actor> actors;
    Map<String, Item> items;
    Map<Pair<Integer, Integer>, String> locationActor;
    Map<Pair<Integer, Integer>, String> locationItems;

    /**
     * @param actorId identifier of the actor
     * @return (x, y) giving x and y position of the actor
     * @throws Exception if can't find
     */
    public Pair<Integer, Integer> actorLocation(String actorId) throws Exception {
        return locationActor.entrySet().stream()
                .filter(entry -> entry.getValue() == actorId)
                .findFirst()
                .orElseThrow(() -> new Exception("Cannot find actor"))
                .getKey();
    }


}
