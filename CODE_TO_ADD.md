# Premium Feature Code to Add Manually

Since bash commands are not executing, copy these code files manually.

## File 1: src/main/kotlin/com/jakubjirak/seleniumiq/reporting/TestReportGenerator.kt

```kotlin
package com.jakubjirak.seleniumiq.reporting

import com.intellij.openapi.project.Project
import com.jakubjirak.seleniumiq.licensing.LicenseManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TestReportGenerator(private val project: Project) {
    private val licenseManager = LicenseManager.getInstance()

    fun generateExecutionReport(testResults: List<TestResult>): String {
        if (!licenseManager.hasFeature("advanced_reporting")) {
            return generateBasicReport(testResults)
        }
        return generateAdvancedReport(testResults)
    }

    private fun generateBasicReport(results: List<TestResult>): String {
        val passed = results.count { it.status == TestStatus.PASSED }
        val failed = results.count { it.status == TestStatus.FAILED }
        val total = results.size

        return """
            |=== Test Execution Report ===
            |Total Tests: $total
            |Passed: $passed
            |Failed: $failed
            |Success Rate: ${if (total > 0) (passed * 100 / total) else 0}%
            |
            |Upgrade to Professional for detailed analytics and trends!
        """.trimMargin()
    }

    private fun generateAdvancedReport(results: List<TestResult>): String {
        val passed = results.count { it.status == TestStatus.PASSED }
        val failed = results.count { it.status == TestStatus.FAILED }
        val skipped = results.count { it.status == TestStatus.SKIPPED }
        val flaky = results.count { it.isFlaky }
        val total = results.size

        val avgDuration = if (results.isNotEmpty()) {
            results.map { it.duration }.average()
        } else 0.0

        val slowTests = results.sortedByDescending { it.duration }.take(5)

        return """
            |=== Advanced Test Execution Report ===
            |Generated: ${LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}
            |
            |Summary:
            |  Total Tests: $total
            |  Passed: $passed (${percentage(passed, total)}%)
            |  Failed: $failed (${percentage(failed, total)}%)
            |  Skipped: $skipped (${percentage(skipped, total)}%)
            |  Flaky Tests: $flaky (${percentage(flaky, total)}%)
            |
            |Performance:
            |  Average Duration: ${String.format("%.2f", avgDuration)}ms
            |  Total Duration: ${results.sumOf { it.duration }}ms
            |
            |Slowest Tests:
            |${slowTests.joinToString("\n") { "  - ${it.name}: ${it.duration}ms" }}
            |
            |Flaky Tests Detected:
            |${results.filter { it.isFlaky }.joinToString("\n") { "  - ${it.name} (${it.flakyCount} occurrences)" }}
        """.trimMargin()
    }

    fun generateTrendReport(historicalData: List<TestExecutionHistory>): String {
        if (!licenseManager.hasFeature("trend_analysis")) {
            return "Trend analysis is available in Team and Enterprise tiers."
        }

        val recentExecutions = historicalData.takeLast(10)
        val successRates = recentExecutions.map { execution ->
            val total = execution.results.size
            val passed = execution.results.count { it.status == TestStatus.PASSED }
            if (total > 0) (passed * 100.0 / total) else 0.0
        }

        val avgSuccessRate = successRates.average()
        val trend = if (successRates.size >= 2) {
            val recent = successRates.takeLast(3).average()
            val older = successRates.take(successRates.size - 3).average()
            when {
                recent > older + 5 -> "Improving ↑"
                recent < older - 5 -> "Declining ↓"
                else -> "Stable →"
            }
        } else "N/A"

        return """
            |=== Test Trend Analysis ===
            |Last 10 Executions:
            |  Average Success Rate: ${String.format("%.2f", avgSuccessRate)}%
            |  Trend: $trend
            |
            |Success Rate History:
            |${successRates.mapIndexed { i, rate -> "  Run ${i + 1}: ${String.format("%.1f", rate)}%" }.joinToString("\n")}
        """.trimMargin()
    }

    private fun percentage(count: Int, total: Int): Int {
        return if (total > 0) (count * 100 / total) else 0
    }
}

data class TestResult(
    val name: String,
    val status: TestStatus,
    val duration: Long,
    val isFlaky: Boolean = false,
    val flakyCount: Int = 0,
    val errorMessage: String? = null
)

enum class TestStatus {
    PASSED, FAILED, SKIPPED
}

data class TestExecutionHistory(
    val timestamp: LocalDateTime,
    val results: List<TestResult>
)
```

## File 2: src/main/kotlin/com/jakubjirak/seleniumiq/performance/PerformanceAnalyzer.kt

```kotlin
package com.jakubjirak.seleniumiq.performance

import com.intellij.openapi.project.Project
import com.jakubjirak.seleniumiq.licensing.LicenseManager
import com.jakubjirak.seleniumiq.reporting.TestResult

class PerformanceAnalyzer(private val project: Project) {
    private val licenseManager = LicenseManager.getInstance()

    fun analyzeTestPerformance(results: List<TestResult>): PerformanceAnalysis {
        if (!licenseManager.hasFeature("performance_insights")) {
            return PerformanceAnalysis.limited()
        }

        val slowThreshold = calculateSlowThreshold(results)
        val slowTests = results.filter { it.duration > slowThreshold }
        
        val bottlenecks = identifyBottlenecks(results)
        val recommendations = generateRecommendations(slowTests, bottlenecks)

        return PerformanceAnalysis(
            slowTests = slowTests,
            averageDuration = results.map { it.duration }.average(),
            totalDuration = results.sumOf { it.duration },
            bottlenecks = bottlenecks,
            recommendations = recommendations
        )
    }

    private fun calculateSlowThreshold(results: List<TestResult>): Long {
        if (results.isEmpty()) return 0L
        
        val durations = results.map { it.duration }.sorted()
        val percentile90Index = (durations.size * 0.9).toInt()
        return durations.getOrNull(percentile90Index) ?: durations.last()
    }

    private fun identifyBottlenecks(results: List<TestResult>): List<PerformanceBottleneck> {
        val bottlenecks = mutableListOf<PerformanceBottleneck>()

        val slowLocatorPattern = results.filter { 
            it.name.contains("findElement") && it.duration > 1000 
        }
        if (slowLocatorPattern.size > results.size * 0.2) {
            bottlenecks.add(
                PerformanceBottleneck(
                    type = "Slow Locators",
                    description = "Multiple tests experiencing slow element location",
                    impact = "High",
                    affectedTests = slowLocatorPattern.size
                )
            )
        }

        val waitPattern = results.filter {
            it.name.contains("wait") && it.duration > 5000
        }
        if (waitPattern.isNotEmpty()) {
            bottlenecks.add(
                PerformanceBottleneck(
                    type = "Excessive Waits",
                    description = "Tests using long wait times",
                    impact = "Medium",
                    affectedTests = waitPattern.size
                )
            )
        }

        return bottlenecks
    }

    private fun generateRecommendations(
        slowTests: List<TestResult>,
        bottlenecks: List<PerformanceBottleneck>
    ): List<String> {
        val recommendations = mutableListOf<String>()

        if (slowTests.isNotEmpty()) {
            recommendations.add("Optimize ${slowTests.size} slow tests - consider parallel execution")
        }

        bottlenecks.forEach { bottleneck ->
            when (bottleneck.type) {
                "Slow Locators" -> {
                    recommendations.add("Use AI-powered locator optimization to improve element lookup speed")
                    recommendations.add("Consider using CSS selectors instead of XPath for better performance")
                }
                "Excessive Waits" -> {
                    recommendations.add("Replace Thread.sleep() with explicit waits")
                    recommendations.add("Use smart wait strategies to reduce test execution time")
                }
            }
        }

        return recommendations
    }

    fun generateOptimizationReport(analysis: PerformanceAnalysis): String {
        return """
            |=== Performance Optimization Report ===
            |
            |Performance Metrics:
            |  Average Test Duration: ${String.format("%.2f", analysis.averageDuration)}ms
            |  Total Execution Time: ${analysis.totalDuration}ms
            |  Slow Tests: ${analysis.slowTests.size}
            |
            |Identified Bottlenecks:
            |${analysis.bottlenecks.joinToString("\n") { 
                "  - ${it.type} (${it.impact} impact): ${it.description} [${it.affectedTests} tests]"
            }}
            |
            |Recommendations:
            |${analysis.recommendations.mapIndexed { i, rec -> "  ${i + 1}. $rec" }.joinToString("\n")}
            |
            |Potential Time Savings: ${calculateTimeSavings(analysis)}ms
        """.trimMargin()
    }

    private fun calculateTimeSavings(analysis: PerformanceAnalysis): Long {
        var savings = 0L
        
        savings += (analysis.slowTests.sumOf { it.duration } * 0.3).toLong()
        
        val waitBottleneck = analysis.bottlenecks.find { it.type == "Excessive Waits" }
        if (waitBottleneck != null) {
            savings += (waitBottleneck.affectedTests * 2500)
        }
        
        return savings
    }
}

data class PerformanceAnalysis(
    val slowTests: List<TestResult>,
    val averageDuration: Double,
    val totalDuration: Long,
    val bottlenecks: List<PerformanceBottleneck>,
    val recommendations: List<String>
) {
    companion object {
        fun limited() = PerformanceAnalysis(
            slowTests = emptyList(),
            averageDuration = 0.0,
            totalDuration = 0L,
            bottlenecks = emptyList(),
            recommendations = listOf("Upgrade to Professional tier for performance insights")
        )
    }
}

data class PerformanceBottleneck(
    val type: String,
    val description: String,
    val impact: String,
    val affectedTests: Int
)
```

## File 3: src/main/kotlin/com/jakubjirak/seleniumiq/cloud/CloudIntegration.kt

```kotlin
package com.jakubjirak.seleniumiq.cloud

import com.intellij.openapi.project.Project
import com.jakubjirak.seleniumiq.licensing.LicenseManager

class CloudIntegrationManager(private val project: Project) {
    private val licenseManager = LicenseManager.getInstance()

    fun getBrowserStackIntegration(): BrowserStackIntegration? {
        if (!licenseManager.hasFeature("cloud_integration")) {
            return null
        }
        return BrowserStackIntegration()
    }

    fun getSauceLabsIntegration(): SauceLabsIntegration? {
        if (!licenseManager.hasFeature("cloud_integration")) {
            return null
        }
        return SauceLabsIntegration()
    }

    fun getLambdaTestIntegration(): LambdaTestIntegration? {
        if (!licenseManager.hasFeature("cloud_integration")) {
            return null
        }
        return LambdaTestIntegration()
    }
}

class BrowserStackIntegration {
    fun generateCapabilities(browser: String, version: String, os: String): String {
        return """
            |DesiredCapabilities caps = new DesiredCapabilities();
            |caps.setCapability("browserName", "$browser");
            |caps.setCapability("browserVersion", "$version");
            |caps.setCapability("os", "$os");
            |caps.setCapability("name", "SeleniumIQ Test");
            |caps.setCapability("build", "Build " + System.getenv("BUILD_NUMBER"));
            |
            |String username = System.getenv("BROWSERSTACK_USERNAME");
            |String accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY");
            |String url = "https://" + username + ":" + accessKey + "@hub-cloud.browserstack.com/wd/hub";
            |
            |WebDriver driver = new RemoteWebDriver(new URL(url), caps);
        """.trimMargin()
    }

    fun getAvailableBrowsers(): List<CloudBrowser> {
        return listOf(
            CloudBrowser("Chrome", listOf("latest", "latest-1", "100")),
            CloudBrowser("Firefox", listOf("latest", "latest-1", "95")),
            CloudBrowser("Safari", listOf("15.0", "14.0")),
            CloudBrowser("Edge", listOf("latest", "latest-1"))
        )
    }
}

class SauceLabsIntegration {
    fun generateCapabilities(browser: String, version: String, platform: String): String {
        return """
            |MutableCapabilities caps = new MutableCapabilities();
            |caps.setCapability("browserName", "$browser");
            |caps.setCapability("browserVersion", "$version");
            |caps.setCapability("platformName", "$platform");
            |
            |MutableCapabilities sauceOptions = new MutableCapabilities();
            |sauceOptions.setCapability("name", "SeleniumIQ Test");
            |sauceOptions.setCapability("build", "Build " + System.getenv("BUILD_NUMBER"));
            |caps.setCapability("sauce:options", sauceOptions);
            |
            |String username = System.getenv("SAUCE_USERNAME");
            |String accessKey = System.getenv("SAUCE_ACCESS_KEY");
            |String url = "https://" + username + ":" + accessKey + "@ondemand.us-west-1.saucelabs.com:443/wd/hub";
            |
            |WebDriver driver = new RemoteWebDriver(new URL(url), caps);
        """.trimMargin()
    }
}

class LambdaTestIntegration {
    fun generateCapabilities(browser: String, version: String, platform: String): String {
        return """
            |DesiredCapabilities caps = new DesiredCapabilities();
            |caps.setCapability("browserName", "$browser");
            |caps.setCapability("version", "$version");
            |caps.setCapability("platform", "$platform");
            |caps.setCapability("build", "SeleniumIQ Build " + System.getenv("BUILD_NUMBER"));
            |caps.setCapability("name", "SeleniumIQ Test");
            |caps.setCapability("network", true);
            |caps.setCapability("console", true);
            |
            |String username = System.getenv("LT_USERNAME");
            |String accessKey = System.getenv("LT_ACCESS_KEY");
            |String url = "https://" + username + ":" + accessKey + "@hub.lambdatest.com/wd/hub";
            |
            |WebDriver driver = new RemoteWebDriver(new URL(url), caps);
        """.trimMargin()
    }
}

data class CloudBrowser(
    val name: String,
    val versions: List<String>
)
```

---

## Instructions

1. Create the directories:
   ```bash
   mkdir -p src/main/kotlin/com/jakubjirak/seleniumiq/reporting
   mkdir -p src/main/kotlin/com/jakubjirak/seleniumiq/performance
   mkdir -p src/main/kotlin/com/jakubjirak/seleniumiq/cloud
   ```

2. Copy each code block above into the corresponding file

3. Build and publish as described in FINAL_STEPS.md
