package com.officelife.actions;

import com.officelife.World;
import com.officelife.actions.Action;

public class DoNothing implements Action {
    @Override
    public void accept(World world) {
        // do nothing, since this is the point of this class
    }
}
