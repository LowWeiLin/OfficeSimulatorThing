import java.util.function.Consumer;

public interface Action extends Consumer {
    void perform(World world);
}
