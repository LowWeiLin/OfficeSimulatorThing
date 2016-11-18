
public class Person implements Actor {
    private String id;
    public String name;


    @Override
    public String id() {
        return id;
    }

    @Override
    public Action act(World state) {
        return null;
    }
}
