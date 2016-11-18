import java.util.EnumMap;
import java.util.EnumSet;
import java.util.UUID;

public class Person implements Actor {
    private String id;
    public String name;

    private EnumMap<ActorNeed, Integer> needs;

    public Person(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;

        this.needs = new EnumMap<>(ActorNeed.class);
        for (ActorNeed need : ActorNeed.values()) {
            this.needs.put(need, 0);
        }
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Action act(World state) {
        Pair<Integer, Integer> location;
        try {
            location = state.actorLocation(id);
        } catch (Exception e) {
            throw new RuntimeException("Unable to get actor location", e);
        }

        Item item = state.items.get(
            state.locationItems.get(location)
        );

        return new DrinkCoffee(this, (CoffeeMachine) item);
    }

    @Override
    public void changeNeed(ActorNeed need, int value) {
        int current = this.needs.get(need);
        this.needs.put(need, current + value);

        System.err.println(need + " changed " + value);
    }
}
