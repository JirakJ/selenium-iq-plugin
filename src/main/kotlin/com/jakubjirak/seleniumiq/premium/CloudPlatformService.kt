package com.jakubjirak.seleniumiq.premium

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class CloudPlatformService(private val project: Project) {
    
    enum class CloudProvider {
        BROWSERSTACK,
        SAUCE_LABS,
        LAMBDA_TEST,
        PERFECTO,
        CROSSBROWSER_TESTING
    }
    
    data class CloudConfig(
        val provider: CloudProvider,
        val username: String,
        val accessKey: String,
        val capabilities: Map<String, Any> = emptyMap()
    )
    
    fun generateBrowserStackCode(username: String, accessKey: String): String {
        return """
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import java.net.URL;

public class BrowserStackTest {
    public static final String USERNAME = "$username";
    public static final String AUTOMATE_KEY = "$accessKey";
    public static final String URL = "https://" + USERNAME + ":" + AUTOMATE_KEY + "@hub-cloud.browserstack.com/wd/hub";
    
    public static void main(String[] args) throws Exception {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("browserName", "Chrome");
        caps.setCapability("browserVersion", "latest");
        caps.setCapability("os", "Windows");
        caps.setCapability("osVersion", "10");
        caps.setCapability("build", "SeleniumIQ Build");
        caps.setCapability("name", "Test from SeleniumIQ");
        caps.setCapability("browserstack.debug", "true");
        caps.setCapability("browserstack.networkLogs", "true");
        
        WebDriver driver = new RemoteWebDriver(new URL(URL), caps);
        
        try {
            driver.get("https://example.com");
            System.out.println("Title: " + driver.getTitle());
        } finally {
            driver.quit();
        }
    }
}
        """.trimIndent()
    }
    
    fun generateSauceLabsCode(username: String, accessKey: String): String {
        return """
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import java.net.URL;
import java.util.Map;

public class SauceLabsTest {
    public static final String USERNAME = "$username";
    public static final String ACCESS_KEY = "$accessKey";
    public static final String URL = "https://" + USERNAME + ":" + ACCESS_KEY + "@ondemand.us-west-1.saucelabs.com:443/wd/hub";
    
    public static void main(String[] args) throws Exception {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("browserName", "Chrome");
        caps.setCapability("browserVersion", "latest");
        caps.setCapability("platformName", "Windows 10");
        caps.setCapability("sauce:options", Map.of(
            "build", "SeleniumIQ Build",
            "name", "Test from SeleniumIQ",
            "screenResolution", "1920x1080",
            "recordVideo", true,
            "recordScreenshots", true,
            "extendedDebugging", true
        ));
        
        WebDriver driver = new RemoteWebDriver(new URL(URL), caps);
        
        try {
            driver.get("https://example.com");
            System.out.println("Title: " + driver.getTitle());
        } finally {
            driver.quit();
        }
    }
}
        """.trimIndent()
    }
    
    fun generateLambdaTestCode(username: String, accessKey: String): String {
        return """
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import java.net.URL;

public class LambdaTestTest {
    public static final String USERNAME = "$username";
    public static final String ACCESS_KEY = "$accessKey";
    public static final String URL = "https://" + USERNAME + ":" + ACCESS_KEY + "@hub.lambdatest.com/wd/hub";
    
    public static void main(String[] args) throws Exception {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("browserName", "Chrome");
        caps.setCapability("browserVersion", "latest");
        caps.setCapability("platform", "Windows 10");
        caps.setCapability("build", "SeleniumIQ Build");
        caps.setCapability("name", "Test from SeleniumIQ");
        caps.setCapability("network", true);
        caps.setCapability("visual", true);
        caps.setCapability("video", true);
        caps.setCapability("console", true);
        
        WebDriver driver = new RemoteWebDriver(new URL(URL), caps);
        
        try {
            driver.get("https://example.com");
            System.out.println("Title: " + driver.getTitle());
        } finally {
            driver.quit();
        }
    }
}
        """.trimIndent()
    }
    
    fun generateParallelExecutionCode(provider: CloudProvider, parallelCount: Int): String {
        val url = when (provider) {
            CloudProvider.BROWSERSTACK -> "https://USERNAME:KEY@hub-cloud.browserstack.com/wd/hub"
            CloudProvider.SAUCE_LABS -> "https://USERNAME:KEY@ondemand.us-west-1.saucelabs.com:443/wd/hub"
            CloudProvider.LAMBDA_TEST -> "https://USERNAME:KEY@hub.lambdatest.com/wd/hub"
            else -> "https://hub.example.com/wd/hub"
        }
        
        return """
import org.testng.annotations.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import java.net.URL;

public class ParallelTests {
    
    @Test(threadPoolSize = $parallelCount, invocationCount = $parallelCount)
    public void parallelTest() throws Exception {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("browserName", "Chrome");
        
        WebDriver driver = new RemoteWebDriver(new URL("$url"), caps);
        
        try {
            driver.get("https://example.com");
            // Your test code here
        } finally {
            driver.quit();
        }
    }
}
        """.trimIndent()
    }
    
    companion object {
        fun getInstance(project: Project): CloudPlatformService {
            return project.getService(CloudPlatformService::class.java)
        }
    }
}
