
public class Main {
    public static void main(String[] args) {

        State state = new State();
        while (true) {
            state = state.transition();
        }

    }
}
