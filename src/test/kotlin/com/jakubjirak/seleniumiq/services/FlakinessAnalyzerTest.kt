/*
 * Copyright (c) 2025 Jakub Jirák. All rights reserved.
 * Licensed under the proprietary license. See LICENSE for details.
 */
package com.jakubjirak.seleniumiq.services

import com.jakubjirak.seleniumiq.models.FlakinessScore
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.lang.reflect.Constructor
import java.lang.reflect.Method

class FlakinessAnalyzerTest {

    private fun createAnalyzer(): FlakinessAnalyzer {
        val constructor: Constructor<FlakinessAnalyzer> =
            FlakinessAnalyzer::class.java.getDeclaredConstructor()
        constructor.isAccessible = true
        return constructor.newInstance()
    }

    private fun countAssertions(analyzer: FlakinessAnalyzer, text: String): Int {
        val method: Method = FlakinessAnalyzer::class.java.getDeclaredMethod(
            "countAssertions", String::class.java
        )
        method.isAccessible = true
        return method.invoke(analyzer, text) as Int
    }

    @Test
    fun `countAssertions counts assert keywords`() {
        val analyzer = createAnalyzer()
        val text = """
            assertEquals("expected", result);
            assertTrue(condition);
            assertFalse(condition);
        """.trimIndent()

        val count = countAssertions(analyzer, text)
        // "assertEquals" matches "assert" and "Assert" and "assertEquals" — it's substring matching
        // Each keyword match adds to count independently
        assertTrue(count >= 3, "Expected at least 3 assertion matches but got $count")
    }

    @Test
    fun `countAssertions returns zero for no assertions`() {
        val analyzer = createAnalyzer()
        assertEquals(0, countAssertions(analyzer, "System.out.println(result);"))
    }

    @Test
    fun `countAssertions counts multiple occurrences`() {
        val analyzer = createAnalyzer()
        val text = "assert x; assert y; assert z;"
        val count = countAssertions(analyzer, text)
        assertTrue(count >= 3)
    }

    @Test
    fun `generateFlakinessReport includes risk level`() {
        val analyzer = createAnalyzer()
        val score = FlakinessScore(
            0.7, "HIGH",
            listOf(
                FlakinessScore.FlakinessIndicator("Thread.sleep() detected", 0.7, "desc", "HIGH")
            ),
            listOf("Replace Thread.sleep()")
        )

        val report = analyzer.generateFlakinessReport(score)

        assertTrue(report.contains("HIGH"))
        assertTrue(report.contains("Flakiness Analysis Report"))
        assertTrue(report.contains("Thread.sleep()"))
        assertTrue(report.contains("Replace Thread.sleep()"))
    }

    @Test
    fun `generateFlakinessReport formats score as percentage`() {
        val analyzer = createAnalyzer()
        val score = FlakinessScore(0.52, "MEDIUM", listOf(), listOf())

        val report = analyzer.generateFlakinessReport(score)

        // Locale-aware: may be "52.0%" or "52,0%" depending on system locale
        assertTrue(report.contains("52") && report.contains("%"),
            "Report should contain score percentage: $report")
    }

    @Test
    fun `generateFlakinessReport includes all indicators`() {
        val analyzer = createAnalyzer()
        val score = FlakinessScore(
            0.8, "HIGH",
            listOf(
                FlakinessScore.FlakinessIndicator("Issue1", 0.5, "Description1", "HIGH"),
                FlakinessScore.FlakinessIndicator("Issue2", 0.3, "Description2", "LOW")
            ),
            listOf("Rec1", "Rec2")
        )

        val report = analyzer.generateFlakinessReport(score)

        assertTrue(report.contains("Issue1"))
        assertTrue(report.contains("Issue2"))
        assertTrue(report.contains("Description1"))
        assertTrue(report.contains("1. Rec1"))
        assertTrue(report.contains("2. Rec2"))
    }

    @Test
    fun `generateFlakinessReport handles empty indicators`() {
        val analyzer = createAnalyzer()
        val score = FlakinessScore(0.0, "LOW", listOf(), listOf())

        val report = analyzer.generateFlakinessReport(score)

        assertTrue(report.contains("LOW"))
        assertTrue(report.contains("0") && report.contains("%"),
            "Report should contain zero score: $report")
    }
}
