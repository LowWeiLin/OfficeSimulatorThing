package com.officelife.knowledge;

import com.officelife.actions.*;
import com.officelife.actors.Actor;
import com.officelife.commodity.Commodity;
import com.officelife.commodity.FakeWork;
import com.officelife.commodity.Food;
import com.officelife.items.Coffee;
import com.officelife.items.CoffeeMachine;
import com.officelife.items.Item;
import com.officelife.locations.Cubicle;
import com.officelife.locations.Default;
import com.officelife.locations.LocationTrait;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

// TODO not static
public class KnowledgeBase {

    public static Class<? extends Action> actionProducing(Commodity commodity) {
        if (commodity.getClass() == Food.class) {
            return DrinkCoffee.class;
        }
        if (commodity.getClass() == FakeWork.class) {
            return PretendToWork.class;
        }
        throw new RuntimeException("not supported");
    }

    public static Class<? extends Action> actionProducing(Class<? extends Item> item) {
        if (item == Coffee.class) {
            return UseCoffeeMachine.class;
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

        return Collections.emptyList();
    }

    public static Class<? extends LocationTrait> locationTraitsRequiredForAction(Class<? extends Action> action) {
        if (action == PretendToWork.class) {
            return Cubicle.class;
        }


        return Default.class;
    }

}
