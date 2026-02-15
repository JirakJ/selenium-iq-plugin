package com.jakubjirak.seleniumiq.services;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.jakubjirak.seleniumiq.models.LocatorSuggestion;

import java.util.ArrayList;
import java.util.List;

@Service(Service.Level.APP)
public final class SelfHealingService {

    public static SelfHealingService getInstance() {
        return ApplicationManager.getApplication().getService(SelfHealingService.class);
    }

    public List<LocatorSuggestion> healBrokenLocator(String originalLocator, String locatorType, String currentPageHtml) {
        List<LocatorSuggestion> healingSuggestions = new ArrayList<>();
        
        AILocatorService aiService = AILocatorService.getInstance();
        
        // Try to find similar elements in current page
        List<LocatorSuggestion> suggestions = aiService.generateLocators(currentPageHtml, "body");
        
        for (LocatorSuggestion suggestion : suggestions) {
            if (isSimilarLocator(originalLocator, suggestion.getLocatorValue())) {
                healingSuggestions.add(new LocatorSuggestion(
                    suggestion.getLocatorType(),
                    suggestion.getLocatorValue(),
                    suggestion.getStabilityScore() * 0.9, // slightly lower confidence for healed locators
                    "Self-healed: " + suggestion.getDescription(),
                    suggestion.getFallbackLocators(),
                    suggestion.getStrategy()
                ));
            }
        }
        
        return healingSuggestions;
    }

    private boolean isSimilarLocator(String original, String candidate) {
        String origLower = original.toLowerCase();
        String candLower = candidate.toLowerCase();
        
        // Check for similar class names
        if (origLower.contains("btn") && candLower.contains("btn")) return true;
        if (origLower.contains("input") && candLower.contains("input")) return true;
        if (origLower.contains("submit") && candLower.contains("submit")) return true;
        
        // Check for similar IDs (non-dynamic part)
        String[] origParts = original.split("[\\d_-]+");
        String[] candParts = candidate.split("[\\d_-]+");
        
        for (String origPart : origParts) {
            for (String candPart : candParts) {
                if (origPart.length() > 3 && candPart.length() > 3 && 
                    origPart.equalsIgnoreCase(candPart)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    public boolean canAutoHeal(String locatorType) {
        // Some locator types are easier to auto-heal than others
        switch (locatorType.toLowerCase()) {
            case "id":
            case "name":
            case "cssselector":
                return true;
            case "xpath":
                return false; // XPath is complex, require manual review
            default:
                return false;
        }
    }

    public String generateHealingReport(List<LocatorSuggestion> originalLocators, 
                                       List<LocatorSuggestion> healedLocators) {
        StringBuilder report = new StringBuilder();
        report.append("Self-Healing Report\n");
        report.append("===================\n\n");
        report.append("Original Locators: ").append(originalLocators.size()).append("\n");
        report.append("Healed Locators: ").append(healedLocators.size()).append("\n");
        report.append("Success Rate: ").append(
            String.format("%.1f%%", (healedLocators.size() * 100.0 / originalLocators.size()))
        ).append("\n\n");
        
        for (int i = 0; i < healedLocators.size(); i++) {
            LocatorSuggestion healed = healedLocators.get(i);
            report.append("Healed #").append(i + 1).append(":\n");
            report.append("  Type: ").append(healed.getLocatorType()).append("\n");
            report.append("  Value: ").append(healed.getLocatorValue()).append("\n");
            report.append("  Confidence: ").append(String.format("%.0f%%", healed.getStabilityScore() * 100)).append("\n");
            report.append("  Stability: ").append(healed.getStabilityLevel()).append("\n\n");
        }
        
        return report.toString();
    }
}
