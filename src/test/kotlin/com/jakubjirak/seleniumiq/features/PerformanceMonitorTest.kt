/*
 * Copyright (c) 2025 Jakub Jirák..
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.seleniumiq.features

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import sun.misc.Unsafe
import java.time.Instant

class PerformanceMonitorTest {

    private fun createMonitor(): PerformanceMonitor {
        // PerformanceMonitor takes a non-null Project param; use Unsafe to allocate
        // without calling the constructor, then initialize the metrics field via reflection.
        val unsafeField = Unsafe::class.java.getDeclaredField("theUnsafe")
        unsafeField.isAccessible = true
        val unsafe = unsafeField.get(null) as Unsafe

        val monitor = unsafe.allocateInstance(PerformanceMonitor::class.java) as PerformanceMonitor

        // Initialize the ConcurrentHashMap 'metrics' field
        val metricsField = PerformanceMonitor::class.java.getDeclaredField("metrics")
        metricsField.isAccessible = true
        metricsField.set(monitor, java.util.concurrent.ConcurrentHashMap<String, MutableList<PerformanceMetric>>())

        return monitor
    }

    @Test
    fun `PerformanceMetric data class stores all fields`() {
        val metric = PerformanceMetric(
            testName = "loginTest",
            executionTime = 1500L,
            locatorTime = 300L,
            waitTime = 200L,
            timestamp = Instant.now()
        )

        assertEquals("loginTest", metric.testName)
        assertEquals(1500L, metric.executionTime)
        assertEquals(300L, metric.locatorTime)
        assertEquals(200L, metric.waitTime)
    }

    @Test
    fun `PerformanceInsight data class stores all fields`() {
        val insight = PerformanceInsight(
            type = InsightType.SLOW_LOCATOR,
            severity = Severity.WARNING,
            message = "Locator is slow",
            suggestion = "Use ID selectors"
        )

        assertEquals(InsightType.SLOW_LOCATOR, insight.type)
        assertEquals(Severity.WARNING, insight.severity)
        assertEquals("Locator is slow", insight.message)
    }

    @Test
    fun `InsightType enum has all expected values`() {
        assertEquals(5, InsightType.values().size)
        assertNotNull(InsightType.SLOW_LOCATOR)
        assertNotNull(InsightType.EXCESSIVE_WAIT)
        assertNotNull(InsightType.INEFFICIENT_SELECTOR)
        assertNotNull(InsightType.NETWORK_DELAY)
        assertNotNull(InsightType.BROWSER_STARTUP)
    }

    @Test
    fun `Severity enum has all expected values`() {
        assertEquals(3, Severity.values().size)
        assertNotNull(Severity.INFO)
        assertNotNull(Severity.WARNING)
        assertNotNull(Severity.CRITICAL)
    }

    @Test
    fun `recordMetric stores metrics`() {
        val monitor = createMonitor()
        val metric = PerformanceMetric("test1", 100L, 50L, 25L, Instant.now())
        monitor.recordMetric(metric)

        val avg = monitor.getAverageExecutionTime("test1")
        assertNotNull(avg)
        assertEquals(100L, avg)
    }

    @Test
    fun `getAverageExecutionTime returns null for unknown test`() {
        val monitor = createMonitor()
        assertNull(monitor.getAverageExecutionTime("nonexistent"))
    }

    @Test
    fun `getAverageExecutionTime computes average correctly`() {
        val monitor = createMonitor()
        monitor.recordMetric(PerformanceMetric("test1", 100L, 50L, 25L, Instant.now()))
        monitor.recordMetric(PerformanceMetric("test1", 200L, 50L, 25L, Instant.now()))
        monitor.recordMetric(PerformanceMetric("test1", 300L, 50L, 25L, Instant.now()))

        assertEquals(200L, monitor.getAverageExecutionTime("test1"))
    }

    @Test
    fun `getPerformanceInsights returns slow locator insight`() {
        val monitor = createMonitor()
        repeat(5) {
            monitor.recordMetric(PerformanceMetric("test1", 5000L, 2000L, 100L, Instant.now()))
        }

        val insights = monitor.getPerformanceInsights("test1")
        assertTrue(insights.any { it.type == InsightType.SLOW_LOCATOR })
    }

    @Test
    fun `getPerformanceInsights returns excessive wait insight`() {
        val monitor = createMonitor()
        repeat(5) {
            monitor.recordMetric(PerformanceMetric("test1", 10000L, 100L, 5000L, Instant.now()))
        }

        val insights = monitor.getPerformanceInsights("test1")
        assertTrue(insights.any { it.type == InsightType.EXCESSIVE_WAIT })
    }

    @Test
    fun `getPerformanceInsights returns empty for unknown test`() {
        val monitor = createMonitor()
        assertTrue(monitor.getPerformanceInsights("nonexistent").isEmpty())
    }

    @Test
    fun `recordMetric caps at 100 entries`() {
        val monitor = createMonitor()
        repeat(105) { i ->
            monitor.recordMetric(PerformanceMetric("test1", i.toLong(), 0L, 0L, Instant.now()))
        }

        val allMetrics = monitor.getAllMetrics()
        assertEquals(100, allMetrics["test1"]?.size)
    }

    @Test
    fun `PerformanceMetric equality works`() {
        val now = Instant.now()
        val m1 = PerformanceMetric("test", 100L, 50L, 25L, now)
        val m2 = PerformanceMetric("test", 100L, 50L, 25L, now)
        assertEquals(m1, m2)
    }
}
