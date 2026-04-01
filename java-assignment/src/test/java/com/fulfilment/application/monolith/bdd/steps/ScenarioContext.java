package com.fulfilment.application.monolith.bdd.steps;

import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Per-scenario shared state injected via Cucumber-PicoContainer.
 *
 * A new instance is created for every scenario, so step definition classes
 * receive the same instance through constructor injection — giving them a
 * clean, isolated shared context without static fields.
 */
public class ScenarioContext {

  /** The last HTTP response captured by any step. */
  public Response response;

  /**
   * Named entity IDs stored during a scenario (e.g. "p4" -> 7).
   * Used when a step creates a resource and a later step references it by alias.
   */
  public final Map<String, Long> namedIds = new HashMap<>();
}
