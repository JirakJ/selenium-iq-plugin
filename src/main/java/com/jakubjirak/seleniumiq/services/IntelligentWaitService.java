package com.jakubjirak.seleniumiq.services;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.psi.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service(Service.Level.APP)
public final class IntelligentWaitService {

    private static final Pattern THREAD_SLEEP_PATTERN = Pattern.compile("Thread\\.sleep\\((\\d+)\\)");

    public static IntelligentWaitService getInstance() {
        return ApplicationManager.getApplication().getService(IntelligentWaitService.class);
    }

    public static class WaitRecommendation {
        private final String originalCode;
        private final String recommendedCode;
        private final String waitType;
        private final String explanation;
        private final int confidenceScore;

        public WaitRecommendation(String originalCode, String recommendedCode, 
                                 String waitType, String explanation, int confidenceScore) {
            this.originalCode = originalCode;
            this.recommendedCode = recommendedCode;
            this.waitType = waitType;
            this.explanation = explanation;
            this.confidenceScore = confidenceScore;
        }

        public String getOriginalCode() {
            return originalCode;
        }

        public String getRecommendedCode() {
            return recommendedCode;
        }

        public String getWaitType() {
            return waitType;
        }

        public String getExplanation() {
            return explanation;
        }

        public int getConfidenceScore() {
            return confidenceScore;
        }
    }

    public List<WaitRecommendation> analyzeWaitStrategies(String testCode) {
        List<WaitRecommendation> recommendations = new ArrayList<>();
        
        // Analyze Thread.sleep patterns
        Matcher matcher = THREAD_SLEEP_PATTERN.matcher(testCode);
        while (matcher.find()) {
            String sleepCall = matcher.group(0);
            int sleepTime = Integer.parseInt(matcher.group(1));
            
            WaitRecommendation recommendation = generateWaitRecommendation(sleepCall, sleepTime, testCode);
            recommendations.add(recommendation);
        }
        
        return recommendations;
    }

    private WaitRecommendation generateWaitRecommendation(String sleepCall, int sleepTime, String context) {
        // Analyze context to determine best wait strategy
        if (context.contains("findElement") && context.contains("click")) {
            return new WaitRecommendation(
                sleepCall,
                generateExplicitWait(sleepTime, "elementToBeClickable"),
                "Explicit Wait - Element Clickable",
                "Element needs to be both visible and enabled before clicking",
                95
            );
        }
        
        if (context.contains("findElement") && (context.contains("getText") || context.contains("text()"))) {
            return new WaitRecommendation(
                sleepCall,
                generateExplicitWait(sleepTime, "visibilityOfElementLocated"),
                "Explicit Wait - Visibility",
                "Wait for element to be present and visible in DOM",
                90
            );
        }
        
        if (context.contains("Ajax") || context.contains("loading") || context.contains("spinner")) {
            return new WaitRecommendation(
                sleepCall,
                generateExplicitWait(sleepTime, "invisibilityOfElementLocated"),
                "Explicit Wait - Invisibility",
                "Wait for loading indicator to disappear",
                85
            );
        }
        
        // Default recommendation
        return new WaitRecommendation(
            sleepCall,
            generateExplicitWait(sleepTime, "presenceOfElementLocated"),
            "Explicit Wait - Presence",
            "Wait for element to be present in DOM",
            75
        );
    }

    private String generateExplicitWait(int timeoutMs, String condition) {
        int timeoutSeconds = (timeoutMs / 1000) + 1; // Round up
        
        return String.format(
            "new WebDriverWait(driver, Duration.ofSeconds(%d))\n" +
            "    .until(ExpectedConditions.%s(locator))",
            timeoutSeconds,
            condition
        );
    }

    public String generateSmartWait(String elementLocator, String action) {
        StringBuilder code = new StringBuilder();
        
        code.append("// Smart Wait - AI Generated\n");
        code.append("WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));\n");
        
        switch (action.toLowerCase()) {
            case "click":
                code.append("WebElement element = wait.until(ExpectedConditions.elementToBeClickable(")
                    .append(elementLocator).append("));\n");
                code.append("element.click();\n");
                break;
            
            case "sendkeys":
            case "type":
                code.append("WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(")
                    .append(elementLocator).append("));\n");
                code.append("element.clear();\n");
                code.append("element.sendKeys(\"your text\");\n");
                break;
            
            case "gettext":
            case "read":
                code.append("WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(")
                    .append(elementLocator).append("));\n");
                code.append("String text = element.getText();\n");
                break;
            
            default:
                code.append("WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(")
                    .append(elementLocator).append("));\n");
        }
        
        return code.toString();
    }

    public String generateAjaxWait() {
        return "// Wait for AJAX calls to complete\n" +
               "new WebDriverWait(driver, Duration.ofSeconds(10))\n" +
               "    .until(webDriver -> ((JavascriptExecutor) webDriver)\n" +
               "        .executeScript(\"return jQuery.active == 0\"));";
    }

    public String generateAngularWait() {
        return "// Wait for Angular to finish rendering\n" +
               "new WebDriverWait(driver, Duration.ofSeconds(10))\n" +
               "    .until(webDriver -> ((JavascriptExecutor) webDriver)\n" +
               "        .executeScript(\"return window.getAllAngularTestabilities()\"\n" +
               "            + \".findIndex(x=>!x.isStable()) === -1\"));";
    }

    public String generateReactWait() {
        return "// Wait for React to finish updates\n" +
               "new WebDriverWait(driver, Duration.ofSeconds(10))\n" +
               "    .until(webDriver -> ((JavascriptExecutor) webDriver)\n" +
               "        .executeScript(\"return document.readyState\").equals(\"complete\"));";
    }

    public String generateCustomWait(String condition) {
        return String.format(
            "// Custom wait condition\n" +
            "new WebDriverWait(driver, Duration.ofSeconds(10))\n" +
            "    .until(webDriver -> %s);",
            condition
        );
    }

    public String optimizeExistingWait(String waitCode) {
        // Optimize existing wait strategies
        if (waitCode.contains("Thread.sleep")) {
            return waitCode.replaceAll(
                "Thread\\.sleep\\((\\d+)\\)",
                "// Replaced with smart wait\n" + 
                "new WebDriverWait(driver, Duration.ofSeconds($1 / 1000))\n" +
                "    .until(ExpectedConditions.presenceOfElementLocated(locator))"
            );
        }
        
        return waitCode;
    }
}
