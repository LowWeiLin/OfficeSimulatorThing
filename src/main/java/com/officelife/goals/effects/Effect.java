package com.officelife.goals.effects;

/**
 * The effect of a goal is what it does to the world state.
 */
public interface Effect {

  /**
   * An effect not having work left results in goal failure.
   */
  boolean hasWorkLeft();

  boolean isTerminal();

  /**
   * Poor man's algebraic data type.
   */
  default TerminalAction getTerminalAction() {
    return (TerminalAction) this;
  }

  default Alternatives getAlternatives() {
    return (Alternatives) this;
  }
}

