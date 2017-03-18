package com.officelife;

import com.officelife.actions.ConsumeItem;
import com.officelife.actors.Person;
import com.officelife.goals.State;
import com.officelife.items.Pants;
import junit.framework.TestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;


public class ConsumeItemTest extends TestCase {

    public void testConsumeItems() {
        World world = new World();
        Person person = new Person("test", 0, 0,0);
        Pants pants = new Pants();
        person.addItem(pants);
        ConsumeItem consume = new ConsumeItem(new State(world, person), Pants.class);

        assertThat(person.inventory(), hasItems(pants));
        consume.accept();

        assertThat(person.inventory(), not(hasItems(pants)));
    }
}
