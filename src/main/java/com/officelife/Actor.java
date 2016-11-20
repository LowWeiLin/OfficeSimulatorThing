package com.officelife;

public interface Actor extends Renderable{
    String id();

    Action act(World state);

    void changeNeed(ActorNeed need, int value);
}
