
public class Main {
    public static void main(String[] args) {

        World state = new World();
        while (true) {
            for (Actor actor : state.actors.values()) {
                Action action = actor.act(state);
                action.perform(state);
            }
        }

    }
}
