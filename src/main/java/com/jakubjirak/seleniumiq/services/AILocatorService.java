package com.jakubjirak.seleniumiq.services;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.jakubjirak.seleniumiq.models.LocatorSuggestion;
import com.jakubjirak.seleniumiq.models.LocatorSuggestion.LocatorStrategy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Service(Service.Level.APP)
public final class AILocatorService {

    private static final Pattern DYNAMIC_ID_PATTERN = Pattern.compile(".*\\d{5,}.*|.*[0-9a-f]{8}-[0-9a-f]{4}-.*");

    public static AILocatorService getInstance() {
        return ApplicationManager.getApplication().getService(AILocatorService.class);
    }

    public List<LocatorSuggestion> generateLocators(String htmlSnippet, String targetElementSelector) {
        List<LocatorSuggestion> suggestions = new ArrayList<>();
        
        try {
            Document doc = Jsoup.parse(htmlSnippet);
            Element target = doc.selectFirst(targetElementSelector);
            
            if (target == null) {
                return suggestions;
            }

            // Strategy 1: ID-based (highest priority if stable)
            if (target.hasAttr("id") && !isDynamicId(target.attr("id"))) {
                suggestions.add(new LocatorSuggestion(
                    "id",
                    target.attr("id"),
                    0.95,
                    "ID-based locator - most stable option",
                    Arrays.asList(
                        "By.cssSelector(\"#" + target.attr("id") + "\")",
                        "By.xpath(\"//*[@id='" + target.attr("id") + "']\")"
                    ),
                    LocatorStrategy.ID
                ));
            }

            // Strategy 2: Name attribute
            if (target.hasAttr("name")) {
                suggestions.add(new LocatorSuggestion(
                    "name",
                    target.attr("name"),
                    0.85,
                    "Name-based locator - good stability",
                    Arrays.asList(
                        "By.cssSelector(\"[name='" + target.attr("name") + "']\")"
                    ),
                    LocatorStrategy.NAME
                ));
            }

            // Strategy 3: data-testid or similar testing attributes
            if (target.hasAttr("data-testid")) {
                suggestions.add(new LocatorSuggestion(
                    "cssSelector",
                    "[data-testid='" + target.attr("data-testid") + "']",
                    0.90,
                    "Test-specific attribute - excellent stability",
                    Arrays.asList(
                        "By.xpath(\"//*[@data-testid='" + target.attr("data-testid") + "']\")"
                    ),
                    LocatorStrategy.CSS_SELECTOR_COMPOUND
                ));
            }

            // Strategy 4: Compound CSS selector with multiple attributes
            String compoundCss = buildCompoundCssSelector(target);
            if (!compoundCss.isEmpty()) {
                suggestions.add(new LocatorSuggestion(
                    "cssSelector",
                    compoundCss,
                    0.75,
                    "Multi-attribute CSS selector - good stability",
                    generateFallbackSelectors(target),
                    LocatorStrategy.CSS_SELECTOR_COMPOUND
                ));
            }

            // Strategy 5: XPath with text content
            if (target.hasText() && !target.text().trim().isEmpty()) {
                String text = target.text().trim();
                if (text.length() < 50) { // Avoid very long text
                    suggestions.add(new LocatorSuggestion(
                        "xpath",
                        "//" + target.tagName() + "[contains(text(), '" + text + "')]",
                        0.70,
                        "Text-based XPath - good for buttons and links",
                        Arrays.asList(
                            "By.xpath(\"//" + target.tagName() + "[text()='" + text + "']\")"
                        ),
                        LocatorStrategy.XPATH_TEXT
                    ));
                }
            }

            // Strategy 6: Relative XPath with parent context
            String relativeXPath = buildRelativeXPath(target);
            suggestions.add(new LocatorSuggestion(
                "xpath",
                relativeXPath,
                0.65,
                "Relative XPath - moderate stability",
                Arrays.asList(),
                LocatorStrategy.XPATH_RELATIVE
            ));

        } catch (Exception e) {
            // Log error but don't crash
            System.err.println("Error generating locators: " + e.getMessage());
        }

        return suggestions;
    }

    private boolean isDynamicId(String id) {
        return DYNAMIC_ID_PATTERN.matcher(id).matches() || 
               id.contains("generated") || 
               id.contains("random");
    }

    private String buildCompoundCssSelector(Element element) {
        StringBuilder selector = new StringBuilder(element.tagName());
        
        if (element.hasAttr("class")) {
            String[] classes = element.attr("class").split("\\s+");
            for (String cls : classes) {
                if (!cls.isEmpty() && !isDynamicClass(cls)) {
                    selector.append(".").append(cls);
                }
            }
        }
        
        if (element.hasAttr("type")) {
            selector.append("[type='").append(element.attr("type")).append("']");
        }
        
        if (element.hasAttr("placeholder")) {
            selector.append("[placeholder='").append(element.attr("placeholder")).append("']");
        }
        
        return selector.toString();
    }

    private boolean isDynamicClass(String className) {
        return className.matches(".*\\d{3,}.*") || 
               className.contains("generated") ||
               className.length() > 40;
    }

    private String buildRelativeXPath(Element element) {
        StringBuilder xpath = new StringBuilder("//");
        xpath.append(element.tagName());
        
        if (element.hasAttr("id") && !isDynamicId(element.attr("id"))) {
            xpath.append("[@id='").append(element.attr("id")).append("']");
        } else if (element.hasAttr("class")) {
            String[] classes = element.attr("class").split("\\s+");
            for (String cls : classes) {
                if (!isDynamicClass(cls)) {
                    xpath.append("[contains(@class, '").append(cls).append("')]");
                    break;
                }
            }
        }
        
        return xpath.toString();
    }

    private List<String> generateFallbackSelectors(Element element) {
        List<String> fallbacks = new ArrayList<>();
        
        if (element.hasAttr("class")) {
            fallbacks.add("By.className(\"" + element.attr("class").split("\\s+")[0] + "\")");
        }
        
        if (element.hasAttr("type")) {
            fallbacks.add("By.cssSelector(\"" + element.tagName() + "[type='" + element.attr("type") + "']\")");
        }
        
        return fallbacks;
    }

    public double calculateStabilityScore(String locator, String locatorType) {
        double score = 0.5; // base score
        
        switch (locatorType.toLowerCase()) {
            case "id":
                score = isDynamicId(locator) ? 0.4 : 0.95;
                break;
            case "name":
                score = 0.85;
                break;
            case "xpath":
                if (locator.startsWith("//html/body")) {
                    score = 0.2; // absolute xpath
                } else if (locator.contains("text()")) {
                    score = 0.7;
                } else {
                    score = 0.6;
                }
                break;
            case "cssselector":
                if (locator.startsWith("[data-testid")) {
                    score = 0.9;
                } else if (locator.contains(".") && locator.contains("[")) {
                    score = 0.75;
                } else {
                    score = 0.65;
                }
                break;
        }
        
        return score;
    }
}
