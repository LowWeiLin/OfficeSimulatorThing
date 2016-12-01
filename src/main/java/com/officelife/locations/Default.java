package com.officelife.locations;

import com.officelife.characteristics.Characteristic;

import java.util.HashSet;
import java.util.Set;

public class Default implements LocationTrait {
    @Override
    public Set<Characteristic> characteristics() {
        return new HashSet<>();
    }
}
