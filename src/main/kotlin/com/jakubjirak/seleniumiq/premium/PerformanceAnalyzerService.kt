package com.jakubjirak.seleniumiq.premium

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.jakubjirak.seleniumiq.licensing.LicenseManager
import com.jakubjirak.seleniumiq.licensing.LicenseTier
import java.time.Duration
import java.time.Instant

@Service(Service.Level.PROJECT)
class PerformanceAnalyzerService(private val project: Project) {
    
    data class PerformanceMetrics(
        val testName: String,
        val executionTime: Duration,
        val locatorTimes: Map<String, Duration>,
        val waitTimes: List<WaitMetric>,
        val slowOperations: List<SlowOperation>,
        val recommendations: List<String>
    )
    
    data class WaitMetric(
        val locator: String,
        val waitType: String,
        val duration: Duration,
        val successful: Boolean
    )
    
    data class SlowOperation(
        val operation: String,
        val locator: String?,
        val duration: Duration,
        val lineNumber: Int
    )
    
    private val performanceData = mutableMapOf<String, MutableList<PerformanceMetrics>>()
    
    fun analyzeTestPerformance(
        testName: String,
        executionTrace: List<ExecutionStep>
    ): PerformanceMetrics {
        val licenseManager = LicenseManager.getInstance(project)
        
        if (licenseManager.getCurrentTier() == LicenseTier.FREE) {
            throw IllegalStateException("Performance analysis requires Individual tier or higher")
        }
        
        val totalTime = executionTrace.lastOrNull()?.timestamp?.let { end ->
            executionTrace.firstOrNull()?.timestamp?.let { start ->
                Duration.between(start, end)
            }
        } ?: Duration.ZERO
        
        val locatorTimes = mutableMapOf<String, Duration>()
        val waitTimes = mutableListOf<WaitMetric>()
        val slowOps = mutableListOf<SlowOperation>()
        
        executionTrace.windowed(2, 1).forEach { (prev, current) ->
            val stepDuration = Duration.between(prev.timestamp, current.timestamp)
            
            when (current.type) {
                ExecutionStepType.FIND_ELEMENT -> {
                    locatorTimes[current.locator ?: "unknown"] = 
                        locatorTimes.getOrDefault(current.locator, Duration.ZERO).plus(stepDuration)
                    
                    if (stepDuration.toMillis() > 1000) {
                        slowOps.add(SlowOperation(
                            operation = "Find Element",
                            locator = current.locator,
                            duration = stepDuration,
                            lineNumber = current.lineNumber
                        ))
                    }
                }
                ExecutionStepType.WAIT -> {
                    waitTimes.add(WaitMetric(
                        locator = current.locator ?: "unknown",
                        waitType = current.waitType ?: "explicit",
                        duration = stepDuration,
                        successful = current.successful
                    ))
                }
                ExecutionStepType.CLICK, ExecutionStepType.SEND_KEYS -> {
                    if (stepDuration.toMillis() > 500) {
                        slowOps.add(SlowOperation(
                            operation = current.type.name,
                            locator = current.locator,
                            duration = stepDuration,
                            lineNumber = current.lineNumber
                        ))
                    }
                }
                ExecutionStepType.NAVIGATE, ExecutionStepType.ASSERT -> {
                    // No special handling for now
                }
            }
        }
        
        val recommendations = generateRecommendations(locatorTimes, waitTimes, slowOps)
        
        val metrics = PerformanceMetrics(
            testName = testName,
            executionTime = totalTime,
            locatorTimes = locatorTimes,
            waitTimes = waitTimes,
            slowOperations = slowOps.sortedByDescending { it.duration },
            recommendations = recommendations
        )
        
        performanceData.getOrPut(testName) { mutableListOf() }.add(metrics)
        
        return metrics
    }
    
    private fun generateRecommendations(
        locatorTimes: Map<String, Duration>,
        waitTimes: List<WaitMetric>,
        slowOps: List<SlowOperation>
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        // Slow locators
        locatorTimes.forEach { (locator, duration) ->
            if (duration.toMillis() > 2000) {
                recommendations.add(
                    "Locator '$locator' is slow (${duration.toMillis()}ms). Consider using ID or CSS selector instead of XPath."
                )
            }
        }
        
        // Excessive waits
        val totalWaitTime = waitTimes.sumOf { it.duration.toMillis() }
        if (totalWaitTime > 10000) {
            recommendations.add(
                "Total wait time is ${totalWaitTime}ms. Review wait conditions to reduce unnecessary delays."
            )
        }
        
        // Failed waits
        val failedWaits = waitTimes.filter { !it.successful }
        if (failedWaits.isNotEmpty()) {
            recommendations.add(
                "${failedWaits.size} wait(s) timed out. Review wait conditions and timeouts."
            )
        }
        
        // Slow operations
        if (slowOps.size > 3) {
            recommendations.add(
                "${slowOps.size} slow operations detected. Consider optimizing locators or page load times."
            )
        }
        
        return recommendations
    }
    
    fun getPerformanceTrends(testName: String, limit: Int = 10): List<PerformanceMetrics> {
        val licenseManager = LicenseManager.getInstance(project)
        
        if (licenseManager.getCurrentTier() == LicenseTier.FREE) {
            throw IllegalStateException("Performance trends require Individual tier or higher")
        }
        
        return performanceData[testName]?.takeLast(limit) ?: emptyList()
    }
    
    fun generatePerformanceReport(): String {
        val licenseManager = LicenseManager.getInstance(project)
        
        if (licenseManager.getCurrentTier() != LicenseTier.TEAM && 
            licenseManager.getCurrentTier() != LicenseTier.ENTERPRISE) {
            throw IllegalStateException("Performance reports require Team tier or higher")
        }
        
        return buildString {
            appendLine("# Performance Analysis Report")
            appendLine()
            
            performanceData.forEach { (testName, metrics) ->
                val avgTime = metrics.map { it.executionTime.toMillis() }.average()
                val latestMetric = metrics.last()
                
                appendLine("## $testName")
                appendLine("- Average Execution Time: ${avgTime.toLong()}ms")
                appendLine("- Latest Execution: ${latestMetric.executionTime.toMillis()}ms")
                appendLine("- Slow Operations: ${latestMetric.slowOperations.size}")
                appendLine()
                
                if (latestMetric.recommendations.isNotEmpty()) {
                    appendLine("**Recommendations:**")
                    latestMetric.recommendations.forEach { rec ->
                        appendLine("- $rec")
                    }
                    appendLine()
                }
            }
        }
    }
    
    data class ExecutionStep(
        val timestamp: Instant,
        val type: ExecutionStepType,
        val locator: String?,
        val waitType: String?,
        val successful: Boolean,
        val lineNumber: Int
    )
    
    enum class ExecutionStepType {
        FIND_ELEMENT,
        CLICK,
        SEND_KEYS,
        WAIT,
        NAVIGATE,
        ASSERT
    }
}
