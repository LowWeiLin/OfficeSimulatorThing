package com.officelife;

import junit.framework.TestCase;
import org.hamcrest.Matchers;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsEmptyCollection.empty;

/**
 *
 *
 */
public class WorldTest extends TestCase {

    public void testFindPath() {
        World world = new World();
        List<Coords> path = world.findPath(new Coords(-3, 0), new World.EndCoords(new Coords(3, 1)));
        assertThat(path, Matchers.anything());
    }
}
