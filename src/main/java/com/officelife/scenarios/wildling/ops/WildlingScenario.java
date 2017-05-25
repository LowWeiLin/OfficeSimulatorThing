package com.officelife.scenarios.wildling.ops;

import com.officelife.core.FirstWorld;
import com.officelife.core.Scenario;
import com.officelife.scenarios.detective.DetectiveDirector;
import com.officelife.scenarios.items.Item;
import com.officelife.scenarios.items.Pants;
import com.officelife.scenarios.items.SharpStick;
import com.officelife.utility.Coords;


public class WildlingScenario implements Scenario {

    private final DetectiveDirector director;

    private final FirstWorld world;

    public WildlingScenario() {
        this.director = new DetectiveDirector();
        this.world = new FirstWorld();
    }

    @Override
    public FirstWorld world() {

        String foodGuyId = "Tormund Giantsbane";
        Coords origin = new Coords(0, 0);
        world.putPersonWithItems(director, foodGuyId,
                origin, 15, 15, 1,
                new SharpStick());

        world.putPersonWithItems(director, "Rattleshirt",
          new Coords(0, 1), 15, 15, 1,
          new Pants());

        world.putActor(new Coords(-1, -2), new FruitTree("Tree"));

        Item pants = new Pants();
        Coords coffeeLocation = new Coords(origin.x + 1, origin.y - 1);

        world.putItems(pants, coffeeLocation);

        return world;
    }
}
