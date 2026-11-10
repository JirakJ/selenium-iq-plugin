package com.jakubjirak.seleniumiq.services;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

import java.util.*;

@Service(Service.Level.APP)
public final class CrossBrowserTestingService {

    public enum Browser {
        CHROME("Chrome", "chromedriver", "chrome"),
        FIREFOX("Firefox", "geckodriver", "firefox"),
        SAFARI("Safari", "safaridriver", "safari"),
        EDGE("Edge", "msedgedriver", "edge");

        private final String displayName;
        private final String driverName;
        private final String capability;

        Browser(String displayName, String driverName, String capability) {
            this.displayName = displayName;
            this.driverName = driverName;
            this.capability = capability;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDriverName() {
            return driverName;
        }

        public String getCapability() {
            return capability;
        }
    }

    public static CrossBrowserTestingService getInstance() {
        return ApplicationManager.getApplication().getService(CrossBrowserTestingService.class);
    }

    public String generateCrossBrowserTest(String testName, List<Browser> browsers) {
        StringBuilder code = new StringBuilder();
        
        code.append("import org.junit.jupiter.params.ParameterizedTest;\n");
        code.append("import org.junit.jupiter.params.provider.EnumSource;\n");
        code.append("import org.openqa.selenium.WebDriver;\n");
        code.append("import org.openqa.selenium.chrome.ChromeDriver;\n");
        code.append("import org.openqa.selenium.firefox.FirefoxDriver;\n");
        code.append("import org.openqa.selenium.safari.SafariDriver;\n");
        code.append("import org.openqa.selenium.edge.EdgeDriver;\n\n");
        
        code.append("public class ").append(testName).append("CrossBrowserTest {\n\n");
        
        code.append("    public enum TestBrowser {\n");
        for (int i = 0; i < browsers.size(); i++) {
            Browser browser = browsers.get(i);
            code.append("        ").append(browser.name());
            if (i < browsers.size() - 1) {
                code.append(",");
            }
            code.append("\n");
        }
        code.append("    }\n\n");
        
        code.append("    @ParameterizedTest\n");
        code.append("    @EnumSource(TestBrowser.class)\n");
        code.append("    public void test").append(testName).append("(TestBrowser browser) {\n");
        code.append("        WebDriver driver = createDriver(browser);\n");
        code.append("        try {\n");
        code.append("            // Your test code here\n");
        code.append("            driver.get(\"https://example.com\");\n");
        code.append("            // Add assertions\n");
        code.append("        } finally {\n");
        code.append("            driver.quit();\n");
        code.append("        }\n");
        code.append("    }\n\n");
        
        code.append("    private WebDriver createDriver(TestBrowser browser) {\n");
        code.append("        switch (browser) {\n");
        for (Browser browser : browsers) {
            code.append("            case ").append(browser.name()).append(":\n");
            code.append("                return new ").append(browser.getDisplayName()).append("Driver();\n");
        }
        code.append("            default:\n");
        code.append("                throw new IllegalArgumentException(\"Unsupported browser: \" + browser);\n");
        code.append("        }\n");
        code.append("    }\n");
        code.append("}\n");
        
        return code.toString();
    }

    public Map<Browser, List<String>> analyzeCrossBrowserCompatibility(String locator, String locatorType) {
        Map<Browser, List<String>> compatibility = new HashMap<>();
        
        for (Browser browser : Browser.values()) {
            List<String> warnings = new ArrayList<>();
            
            // Browser-specific compatibility checks
            if (locatorType.equals("cssSelector")) {
                if (browser == Browser.SAFARI && locator.contains(":has(")) {
                    warnings.add("Safari has limited support for :has() selector");
                }
                if (browser == Browser.EDGE && locator.contains("::part")) {
                    warnings.add("Edge may have issues with ::part selector in older versions");
                }
            }
            
            if (locatorType.equals("xpath")) {
                if (browser == Browser.FIREFOX && locator.contains("following-sibling")) {
                    warnings.add("Firefox may be slower with following-sibling axis");
                }
            }
            
            compatibility.put(browser, warnings);
        }
        
        return compatibility;
    }

    public String generateBrowserSpecificLocator(String genericLocator, Browser browser) {
        // Optimize locator for specific browser
        switch (browser) {
            case CHROME:
                // Chrome supports all modern selectors
                return genericLocator;
            
            case FIREFOX:
                // Firefox optimization
                if (genericLocator.startsWith("//")) {
                    // Convert complex XPath to CSS when possible
                    return optimizeForFirefox(genericLocator);
                }
                return genericLocator;
            
            case SAFARI:
                // Safari may need fallbacks
                return optimizeForSafari(genericLocator);
            
            case EDGE:
                // Edge optimization
                return optimizeForEdge(genericLocator);
            
            default:
                return genericLocator;
        }
    }

    private String optimizeForFirefox(String xpath) {
        // Simplify XPath for better Firefox performance
        if (xpath.contains("descendant::")) {
            return xpath.replace("descendant::", "//");
        }
        return xpath;
    }

    private String optimizeForSafari(String locator) {
        // Safari-specific optimizations
        if (locator.contains(":has(")) {
            // Fallback for unsupported selectors
            return locator.replaceAll(":has\\([^)]+\\)", "");
        }
        return locator;
    }

    private String optimizeForEdge(String locator) {
        // Edge-specific optimizations
        return locator;
    }

    public String generateBrowserStackIntegration(String testName, List<Browser> browsers) {
        StringBuilder code = new StringBuilder();
        
        code.append("import org.openqa.selenium.remote.RemoteWebDriver;\n");
        code.append("import org.openqa.selenium.remote.DesiredCapabilities;\n");
        code.append("import java.net.URL;\n\n");
        
        code.append("// BrowserStack Integration\n");
        code.append("public class ").append(testName).append("BrowserStackTest {\n\n");
        
        code.append("    private static final String USERNAME = System.getenv(\"BROWSERSTACK_USERNAME\");\n");
        code.append("    private static final String ACCESS_KEY = System.getenv(\"BROWSERSTACK_ACCESS_KEY\");\n");
        code.append("    private static final String URL = \"https://\" + USERNAME + \":\" + ACCESS_KEY + \"@hub-cloud.browserstack.com/wd/hub\";\n\n");
        
        for (Browser browser : browsers) {
            code.append("    public WebDriver create").append(browser.getDisplayName()).append("Driver() throws Exception {\n");
            code.append("        DesiredCapabilities caps = new DesiredCapabilities();\n");
            code.append("        caps.setCapability(\"browserName\", \"").append(browser.getDisplayName()).append("\");\n");
            code.append("        caps.setCapability(\"browserVersion\", \"latest\");\n");
            code.append("        caps.setCapability(\"os\", \"Windows\");\n");
            code.append("        caps.setCapability(\"osVersion\", \"11\");\n");
            code.append("        return new RemoteWebDriver(new URL(URL), caps);\n");
            code.append("    }\n\n");
        }
        
        code.append("}\n");
        
        return code.toString();
    }
}
