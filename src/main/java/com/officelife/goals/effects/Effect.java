package com.officelife.goals.effects;

/**
 * Poor man's algebraic data type
 */
public interface Effect {

  boolean hasWorkLeft();

  boolean isTerminal();

  default TerminalAction getTerminalAction() {
    return (TerminalAction) this;
  }

  default Alternatives getAlternatives() {
    return (Alternatives) this;
  }
}

