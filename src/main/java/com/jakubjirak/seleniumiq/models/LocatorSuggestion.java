package com.jakubjirak.seleniumiq.models;

import java.util.List;

public class LocatorSuggestion {
    private final String locatorType;
    private final String locatorValue;
    private final double stabilityScore;
    private final String description;
    private final List<String> fallbackLocators;
    private final LocatorStrategy strategy;

    public enum LocatorStrategy {
        ID("id", "Most stable - element has unique ID"),
        NAME("name", "Stable - element has unique name attribute"),
        CSS_CLASS("cssSelector", "Moderate - uses class names"),
        LINK_TEXT("linkText", "Stable for links - exact text match"),
        PARTIAL_LINK_TEXT("partialLinkText", "Moderate for links - partial text match"),
        TAG_NAME("tagName", "Unstable - too generic"),
        XPATH_ABSOLUTE("xpath", "Very unstable - absolute path"),
        XPATH_RELATIVE("xpath", "Moderate - relative path with attributes"),
        XPATH_TEXT("xpath", "Stable - based on text content"),
        CSS_SELECTOR_COMPOUND("cssSelector", "Stable - multiple attributes");

        private final String seleniumMethod;
        private final String description;

        LocatorStrategy(String seleniumMethod, String description) {
            this.seleniumMethod = seleniumMethod;
            this.description = description;
        }

        public String getSeleniumMethod() {
            return seleniumMethod;
        }

        public String getDescription() {
            return description;
        }
    }

    public LocatorSuggestion(String locatorType, String locatorValue, 
                           double stabilityScore, String description,
                           List<String> fallbackLocators, LocatorStrategy strategy) {
        this.locatorType = locatorType;
        this.locatorValue = locatorValue;
        this.stabilityScore = stabilityScore;
        this.description = description;
        this.fallbackLocators = fallbackLocators;
        this.strategy = strategy;
    }

    public String getLocatorType() {
        return locatorType;
    }

    public String getLocatorValue() {
        return locatorValue;
    }

    public double getStabilityScore() {
        return stabilityScore;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getFallbackLocators() {
        return fallbackLocators;
    }

    public LocatorStrategy getStrategy() {
        return strategy;
    }

    public String getSeleniumCode() {
        return String.format("driver.findElement(By.%s(\"%s\"))", 
            locatorType, locatorValue);
    }

    public String getStabilityLevel() {
        if (stabilityScore >= 0.8) return "Excellent";
        if (stabilityScore >= 0.6) return "Good";
        if (stabilityScore >= 0.4) return "Moderate";
        return "Poor";
    }
}
