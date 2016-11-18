
public interface Actor {
    String id();
    Action act(World state);

    void changeNeed(ActorNeed need, int value);
}
