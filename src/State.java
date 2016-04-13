import java.util.List;

public class State {
    private List<Event> events = new ArrayList<Events>();
    public State transition() {
        return this;
    }
}
