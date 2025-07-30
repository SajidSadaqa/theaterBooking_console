package org.example.config;

public class TheaterConfig {
    private final int id;
    private final String configKey;
    private final String configValue;
    private final String description;

    public TheaterConfig(int id, String configKey, String configValue, String description) {
        this.id = id;
        this.configKey = configKey;
        this.configValue = configValue;
        this.description = description;
    }

    public int getId() { return id; }
    public String getConfigKey() { return configKey; }
    public String getConfigValue() { return configValue; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return configKey + " = " + configValue + " (" + description + ")";
    }
}
