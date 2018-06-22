package heroes;

/**
 *
 */
public class Test {

  interface Animal {}
  interface Person {}
  interface Item {}

  static class P<T> {
    // every instance is equal
  }

  static class Functor<T> extends P<T> {
    final String symbol;

    Functor(String symbol) {
      this.symbol = symbol;
    }
  }

  static class Owns extends Functor<Item> {
    final P<Item> item;
    Owns(P<Item> item) {
      super("owns");
      this.item = item;
    }
  }

  static class Not<T> extends P<T> {
    P<T> prop;

    public Not(P<T> prop) {
      this.prop = prop;
    }
  }

  static Owns owns(P<Item> prop) {
    return new Owns(prop);
  }

  static <T> P<T> neg(P<T> prop) {
    if (prop instanceof Not) {
      return ((Not<T>) prop).prop;
    }
    return new Not<>(prop);
  }

  static <T extends Animal> void m(P<T> a) {
  }

  static void anything(P<?> a) {
  }

  static <T extends Animal & Person> void m1(P<T> a) {
  }

  static void implies(P<?> head, P<?>... body) {
  }

  public static <T extends Animal & Person> void main(String[] args) {

    P<Animal> dog = new P<>();
    P<Person> bob = new P<>();
    P<T> beastBoy = new P<>();

    // basic case
    m(dog);

    // using an intersection
    m1(beastBoy);

    // forgetting half the type
    m(beastBoy);

    // accept anything
    anything(dog);
    anything(bob);

    P justice = new P<>();
    P disguise = new P<>();

    implies(justice, disguise, dog);

    // functors
    P<Item> stick = new P<>();
    implies(justice, owns(stick), dog);

    System.out.println("lol");
  }

}
