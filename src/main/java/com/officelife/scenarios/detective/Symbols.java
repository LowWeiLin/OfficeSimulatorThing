package com.officelife.scenarios.detective;

public final class Symbols {

  private Symbols() {}

  /**
   * Meta
   */

  // Refers to the current actor
  public static final String actor = "actor";

  // Owns things owned by no actor
  public static final String ground = "ground";

  /**
   * People
   */

  // Relationships between people
  public static final String hates = "hates";

  // For experiences, emotions, adjectives, etc.
  public static final String feels = "feels";

  /**
   * Adjectives
   */

  public static final String full = "full";

  /**
   * Nouns and objects
   */

  // Types of objects
  public static final String weapon = "weapon";
  public static final String edible = "edible";

  public static final String apple = "apple";
  public static final String crowbar = "crowbar";

  /**
   * Descriptive
   */

  // Inventory membership
  public static final String has = "has";

  // Puts objects into classes, gives them properties, etc.
  public static final String is = "is";
}
