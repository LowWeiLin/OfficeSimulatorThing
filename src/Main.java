
public class Main {
    public static void main(String[] args) {

        World state = initWorld();

        while (true) {
            for (Actor actor : state.actors.values()) {
                Action action = actor.act(state);
                action.accept(state);
            }
        }
    }

    private static World initWorld() {
        World state = new World();
        Actor coffeeDrinker = new Person("Coffee guy");

        Pair<Integer, Integer> origin = new Pair<Integer, Integer>(0, 0);
        state.locationActor.put(origin, coffeeDrinker.id());
        state.actors.put(coffeeDrinker.id(), coffeeDrinker);

        Item coffeeMachine = new CoffeeMachine();
        state.locationItems.put(origin, coffeeMachine.id());
        state.items.put(coffeeMachine.id(), coffeeMachine);
        return state;
    }
}
