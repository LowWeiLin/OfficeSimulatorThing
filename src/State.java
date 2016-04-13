import java.util.ArrayList;
import java.util.List;

public class State {
    private List<Event> events = new ArrayList<>();
    public State transition() {
        return this;
    }
}
