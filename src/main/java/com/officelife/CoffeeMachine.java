package com.officelife;

public class CoffeeMachine implements Item {
    @Override
    public String id() {
        // TODO the office can have more than 1 coffee machine
        return "Coffee_Machine_1";
    }
}