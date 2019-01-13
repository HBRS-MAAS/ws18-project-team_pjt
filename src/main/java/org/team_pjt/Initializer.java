package org.team_pjt;

public abstract class Initializer {
    final static String configPath = "src/main/resources/config/";
    public abstract String initialize(String scenarioDirectory);
}
