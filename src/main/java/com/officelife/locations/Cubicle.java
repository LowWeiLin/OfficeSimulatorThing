package com.officelife.locations;


import com.officelife.characteristics.Characteristic;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Cubicle implements LocationTrait {
    @Override
    public Set<Characteristic> characteristics() {
        Set<Characteristic> characteristics = new HashSet<>();
        characteristics.add(new Characteristic("safety", 1));
        return characteristics;
    }
}
