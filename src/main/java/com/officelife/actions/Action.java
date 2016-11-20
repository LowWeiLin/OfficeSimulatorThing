package com.officelife.actions;

import com.officelife.World;

import java.util.function.Consumer;

/**
 * In the current architecture, {@code #accept} should mutate the World directly.
 */
public interface Action extends Consumer<World> {

}
