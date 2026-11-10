/*
 * Copyright (c) 2025 Jakub Jirák..
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.seleniumiq.models

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FlakinessScoreTest {

    @Test
    fun `calculateScore returns LOW risk when no issues detected`() {
        val metrics = FlakinessScore.TestMetrics()
        val score = FlakinessScore.calculateScore(metrics)

        assertEquals(0.0, score.overallScore, 0.001)
        assertEquals("LOW", score.riskLevel)
        assertTrue(score.indicators.isEmpty())
        assertTrue(score.recommendations.isEmpty())
    }

    @Test
    fun `calculateScore detects Thread sleep and returns HIGH indicator`() {
        val metrics = FlakinessScore.TestMetrics().apply {
            setHasThreadSleep(true)
        }
        val score = FlakinessScore.calculateScore(metrics)

        assertTrue(score.overallScore > 0.0)
        assertEquals(1, score.indicators.size)
        assertEquals("Thread.sleep() detected", score.indicators[0].name)
        assertEquals("HIGH", score.indicators[0].severity)
        assertEquals(0.7, score.indicators[0].score, 0.001)
        assertTrue(score.recommendations.any { it.contains("WebDriverWait") })
    }

    @Test
    fun `calculateScore detects absolute XPath`() {
        val metrics = FlakinessScore.TestMetrics().apply {
            setHasAbsoluteXPath(true)
        }
        val score = FlakinessScore.calculateScore(metrics)

        assertTrue(score.indicators.any { it.name.contains("XPath") })
        assertTrue(score.recommendations.any { it.contains("relative XPath") })
    }

    @Test
    fun `calculateScore detects dynamic IDs with MEDIUM severity`() {
        val metrics = FlakinessScore.TestMetrics().apply {
            setHasDynamicIds(true)
        }
        val score = FlakinessScore.calculateScore(metrics)

        val indicator = score.indicators.first { it.name.contains("Dynamic") }
        assertEquals("MEDIUM", indicator.severity)
        assertEquals(0.5, indicator.score, 0.001)
    }

    @Test
    fun `calculateScore flags high assertion count`() {
        val metrics = FlakinessScore.TestMetrics().apply {
            setAssertionCount(15)
        }
        val score = FlakinessScore.calculateScore(metrics)

        assertTrue(score.indicators.any { it.name.contains("assertion count") })
        assertTrue(score.recommendations.any { it.contains("splitting") })
    }

    @Test
    fun `calculateScore normalizes score to max 1_0`() {
        val metrics = FlakinessScore.TestMetrics().apply {
            setHasThreadSleep(true)
            setHasAbsoluteXPath(true)
            setHasDynamicIds(true)
            setAssertionCount(20)
        }
        val score = FlakinessScore.calculateScore(metrics)

        assertTrue(score.overallScore <= 1.0)
        assertEquals("HIGH", score.riskLevel)
        assertEquals(4, score.indicators.size)
    }

    @Test
    fun `calculateScore returns MEDIUM risk for moderate issues`() {
        // 0.6 / 2.5 = 0.24 → normalized = 0.24 (LOW)
        // 0.7 / 2.5 = 0.28 → still LOW
        // (0.7 + 0.6) / 2.5 = 0.52 → MEDIUM
        val metrics = FlakinessScore.TestMetrics().apply {
            setHasThreadSleep(true)
            setHasAbsoluteXPath(true)
        }
        val score = FlakinessScore.calculateScore(metrics)

        assertEquals("MEDIUM", score.riskLevel)
    }

    @Test
    fun `TestMetrics defaults are all false and zero`() {
        val metrics = FlakinessScore.TestMetrics()

        assertFalse(metrics.hasThreadSleep())
        assertFalse(metrics.hasAbsoluteXPath())
        assertFalse(metrics.hasDynamicIds())
        assertEquals(0, metrics.assertionCount)
    }

    @Test
    fun `FlakinessIndicator stores all properties correctly`() {
        val indicator = FlakinessScore.FlakinessIndicator("test-name", 0.5, "test-desc", "MEDIUM")

        assertEquals("test-name", indicator.name)
        assertEquals(0.5, indicator.score, 0.001)
        assertEquals("test-desc", indicator.description)
        assertEquals("MEDIUM", indicator.severity)
    }
}
