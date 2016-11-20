package com.officelife.commodity;

import com.officelife.characteristics.Characteristic;
import com.officelife.actors.Actor;

import java.util.Set;

/**
 * A commodity has characteristics, which are used to modify actors' needs and
 * work towards goals.
 */
public interface Commodity {
    void applyToPerson(Actor actor);

    Set<Characteristic> characteristics();
}
