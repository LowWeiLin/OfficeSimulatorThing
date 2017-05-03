package com.officelife.utility;

import java.util.Optional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.officelife.core.FirstWorld;

public class Scripting {

  private final ScriptEngine engine;

  public Scripting(FirstWorld world) {
    engine = new ScriptEngineManager().getEngineByName("JavaScript");
    engine.put("world", world);
  }

  public String run(String js) {
    try {
      Optional<Object> result = Optional.ofNullable(engine.eval(js));
      return result.map(Object::toString)
        .orElse("undefined");
    } catch (ScriptException e) {
      return e.getMessage();
    }
  }
}
