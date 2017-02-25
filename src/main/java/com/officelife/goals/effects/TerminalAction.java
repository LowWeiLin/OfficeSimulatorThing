package com.officelife.goals.effects;

import com.officelife.actions.Action;

public class TerminalAction implements Effect {

  public final Action action;

  public TerminalAction(Action action) {
    this.action = action;
  }

  @Override
  public boolean isTerminal() {
    return true;
  }

  @Override
  public boolean hasWorkLeft() {
    return action != null;
  }
}
