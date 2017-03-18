package com.officelife;

import junit.framework.TestCase;
import org.hamcrest.Matchers;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;


/**
 *
 *
 */
public class WorldTest extends TestCase {

    public void testFindPathReturns() {
        World world = new World();
        List<Coords> path = world.findPath(new Coords(-3, 0), new World.EndCoords(new Coords(3, 1))).get();
        assertThat(path, Matchers.anything());
    }

    public void testFindPathTerminatesBeforeMaxSteps() {
        World world = new World();

        boolean zeroStep = world.findPath(new Coords(0, 0), new World.EndCoords(new Coords(0, 0)), 0)
                .isPresent();
        assertThat(zeroStep, Matchers.is(true));

        boolean singleStep = world.findPath(new Coords(0, 0), new World.EndCoords(new Coords(0, 1)), 1)
                .isPresent();
        assertThat(singleStep, Matchers.is(true));

        boolean twoSteps = world.findPath(new Coords(0, 0), new World.EndCoords(new Coords(0, 2)), 1)
                .isPresent();
        assertThat(twoSteps, Matchers.is(false));
    }
}
