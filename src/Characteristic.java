/**
 * Created by user on 18/11/2016.
 */
public class Characteristic {
    private final String name;
    private final int value;

    public Characteristic(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public int value() {
        return value;
    }

}
