package com.officelife.items;

/**
 * This should produce {@code Coffee}.
 * @see Coffee
 */
public class CoffeeMachine implements Item {
    @Override
    public String id() {
        // TODO the office can have more than 1 coffee machine
        return "Coffee_Machine_1";
    }
}
