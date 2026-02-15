package com.jakubjirak.seleniumiq.models;

import java.util.ArrayList;
import java.util.List;

public class FlakinessScore {
    private final double overallScore;
    private final String riskLevel;
    private final List<FlakinessIndicator> indicators;
    private final List<String> recommendations;

    public FlakinessScore(double overallScore, String riskLevel, 
                         List<FlakinessIndicator> indicators, 
                         List<String> recommendations) {
        this.overallScore = overallScore;
        this.riskLevel = riskLevel;
        this.indicators = indicators;
        this.recommendations = recommendations;
    }

    public double getOverallScore() {
        return overallScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public List<FlakinessIndicator> getIndicators() {
        return indicators;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public static class FlakinessIndicator {
        private final String name;
        private final double score;
        private final String description;
        private final String severity;

        public FlakinessIndicator(String name, double score, String description, String severity) {
            this.name = name;
            this.score = score;
            this.description = description;
            this.severity = severity;
        }

        public String getName() {
            return name;
        }

        public double getScore() {
            return score;
        }

        public String getDescription() {
            return description;
        }

        public String getSeverity() {
            return severity;
        }
    }

    public static FlakinessScore calculateScore(TestMetrics metrics) {
        List<FlakinessIndicator> indicators = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        double totalScore = 0.0;

        // Check for Thread.sleep usage
        if (metrics.hasThreadSleep()) {
            indicators.add(new FlakinessIndicator(
                "Thread.sleep() detected",
                0.7,
                "Hard-coded sleeps are unreliable and slow",
                "HIGH"
            ));
            recommendations.add("Replace Thread.sleep() with explicit waits (WebDriverWait)");
            totalScore += 0.7;
        }

        // Check for brittle XPath
        if (metrics.hasAbsoluteXPath()) {
            indicators.add(new FlakinessIndicator(
                "Absolute XPath detected",
                0.6,
                "Absolute XPaths break easily with UI changes",
                "HIGH"
            ));
            recommendations.add("Use relative XPath or CSS selectors with stable attributes");
            totalScore += 0.6;
        }

        // Check for dynamic IDs
        if (metrics.hasDynamicIds()) {
            indicators.add(new FlakinessIndicator(
                "Dynamic ID patterns detected",
                0.5,
                "IDs containing numbers or timestamps are unstable",
                "MEDIUM"
            ));
            recommendations.add("Use data-testid attributes or stable class names");
            totalScore += 0.5;
        }

        // Check test complexity
        if (metrics.getAssertionCount() > 10) {
            indicators.add(new FlakinessIndicator(
                "High assertion count",
                0.3,
                "Tests with many assertions are harder to maintain",
                "LOW"
            ));
            recommendations.add("Consider splitting into smaller, focused tests");
            totalScore += 0.3;
        }

        // Normalize score
        double normalizedScore = Math.min(totalScore / 2.5, 1.0);
        String riskLevel = getRiskLevel(normalizedScore);

        return new FlakinessScore(normalizedScore, riskLevel, indicators, recommendations);
    }

    private static String getRiskLevel(double score) {
        if (score >= 0.7) return "HIGH";
        if (score >= 0.4) return "MEDIUM";
        return "LOW";
    }

    public static class TestMetrics {
        private boolean hasThreadSleep;
        private boolean hasAbsoluteXPath;
        private boolean hasDynamicIds;
        private int assertionCount;

        public boolean hasThreadSleep() {
            return hasThreadSleep;
        }

        public void setHasThreadSleep(boolean hasThreadSleep) {
            this.hasThreadSleep = hasThreadSleep;
        }

        public boolean hasAbsoluteXPath() {
            return hasAbsoluteXPath;
        }

        public void setHasAbsoluteXPath(boolean hasAbsoluteXPath) {
            this.hasAbsoluteXPath = hasAbsoluteXPath;
        }

        public boolean hasDynamicIds() {
            return hasDynamicIds;
        }

        public void setHasDynamicIds(boolean hasDynamicIds) {
            this.hasDynamicIds = hasDynamicIds;
        }

        public int getAssertionCount() {
            return assertionCount;
        }

        public void setAssertionCount(int assertionCount) {
            this.assertionCount = assertionCount;
        }
    }
}
