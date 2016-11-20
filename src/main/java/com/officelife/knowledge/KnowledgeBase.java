package com.officelife.knowledge;

import com.officelife.actions.Action;
import com.officelife.actions.DrinkCoffee;
import com.officelife.actions.UseCoffeeMachine;
import com.officelife.commodity.Commodity;
import com.officelife.commodity.Food;
import com.officelife.items.Coffee;
import com.officelife.items.CoffeeMachine;
import com.officelife.items.Item;

import java.util.Arrays;
import java.util.Collection;

// TODO not static
public class KnowledgeBase {

    public static Class<? extends Action> actionProducing(Commodity commodity) {
        if (commodity.getClass() == Food.class) {
            return DrinkCoffee.class;
        }
        throw new RuntimeException("not supported");
    }

    public static Class<? extends Action> actionProducing(Item item) {
        if (item.getClass() == Coffee.class) {
            return DrinkCoffee.class;
        }
        throw new RuntimeException("not supported");
    }

    public static Collection<Class<? extends Item>> itemsRequiredForAction(Class<? extends Action> action) {
        if (action == DrinkCoffee.class) {
            return Arrays.asList(
                    Coffee.class
            );
        }

        if (action == UseCoffeeMachine.class) {
            return Arrays.asList(
                    CoffeeMachine.class
            );
        }
        throw new RuntimeException("action not supported");
    }

}
