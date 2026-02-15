package com.jakubjirak.seleniumiq.premium

import com.intellij.openapi.project.Project
import com.jakubjirak.seleniumiq.licensing.LicenseManager
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

data class PerformanceMetric(
    val testName: String,
    val executionTime: Long,
    val locatorTime: Long,
    val waitTime: Long,
    val timestamp: Instant
)

data class PerformanceInsight(
    val type: InsightType,
    val severity: Severity,
    val message: String,
    val suggestion: String
)

enum class InsightType {
    SLOW_LOCATOR,
    EXCESSIVE_WAIT,
    INEFFICIENT_SELECTOR,
    NETWORK_DELAY,
    BROWSER_STARTUP
}

enum class Severity {
    INFO,
    WARNING,
    CRITICAL
}

class PerformanceMonitor(private val project: Project) {
    private val metrics = ConcurrentHashMap<String, MutableList<PerformanceMetric>>()
    private val licenseManager = LicenseManager.getInstance()
    
    fun recordMetric(metric: PerformanceMetric) {
        if (!licenseManager.hasFeature("performance_monitoring")) {
            return
        }
        
        metrics.computeIfAbsent(metric.testName) { mutableListOf() }.add(metric)
        
        // Keep only last 100 runs per test
        val testMetrics = metrics[metric.testName]
        if (testMetrics != null && testMetrics.size > 100) {
            testMetrics.removeAt(0)
        }
    }
    
    fun getPerformanceInsights(testName: String): List<PerformanceInsight> {
        if (!licenseManager.hasFeature("performance_monitoring")) {
            return emptyList()
        }
        
        val testMetrics = metrics[testName] ?: return emptyList()
        if (testMetrics.isEmpty()) return emptyList()
        
        val insights = mutableListOf<PerformanceInsight>()
        
        // Analyze slow locators
        val avgLocatorTime = testMetrics.map { it.locatorTime }.average()
        if (avgLocatorTime > 1000) {
            insights.add(PerformanceInsight(
                type = InsightType.SLOW_LOCATOR,
                severity = Severity.WARNING,
                message = "Average locator time is ${avgLocatorTime.toInt()}ms",
                suggestion = "Consider using more specific selectors like ID or data-testid attributes"
            ))
        }
        
        // Analyze excessive waits
        val avgWaitTime = testMetrics.map { it.waitTime }.average()
        if (avgWaitTime > 3000) {
            insights.add(PerformanceInsight(
                type = InsightType.EXCESSIVE_WAIT,
                severity = Severity.CRITICAL,
                message = "Average wait time is ${avgWaitTime.toInt()}ms",
                suggestion = "Replace Thread.sleep() with explicit waits or reduce timeout values"
            ))
        }
        
        // Analyze execution time trend
        if (testMetrics.size >= 10) {
            val recent = testMetrics.takeLast(5).map { it.executionTime }.average()
            val older = testMetrics.takeLast(10).take(5).map { it.executionTime }.average()
            
            if (recent > older * 1.5) {
                insights.add(PerformanceInsight(
                    type = InsightType.NETWORK_DELAY,
                    severity = Severity.WARNING,
                    message = "Test execution time increased by ${((recent - older) / older * 100).toInt()}%",
                    suggestion = "Check for network issues or resource contention"
                ))
            }
        }
        
        return insights
    }
    
    fun getAverageExecutionTime(testName: String): Long? {
        val testMetrics = metrics[testName] ?: return null
        return if (testMetrics.isNotEmpty()) {
            testMetrics.map { it.executionTime }.average().toLong()
        } else null
    }
    
    fun getAllMetrics(): Map<String, List<PerformanceMetric>> {
        return if (licenseManager.hasFeature("performance_monitoring")) {
            metrics.toMap()
        } else {
            emptyMap()
        }
    }
    
    companion object {
        private val instances = ConcurrentHashMap<Project, PerformanceMonitor>()
        
        fun getInstance(project: Project): PerformanceMonitor {
            return instances.computeIfAbsent(project) { PerformanceMonitor(it) }
        }
    }
}
