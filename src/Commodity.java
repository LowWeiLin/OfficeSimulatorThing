import java.util.Set;

/**
 *
 */
public interface Commodity {
    void applyToPerson(Actor actor);
    Set<Characteristic> characteristics();
}
