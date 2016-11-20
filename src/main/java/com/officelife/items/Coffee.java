package com.officelife.items;

import java.util.UUID;

public class Coffee implements Item {
    private final String id;

    public Coffee() {
        id = "Coffee_" + UUID.randomUUID().toString();
    }

    @Override
    public String id() {
        return id;
    }
}
