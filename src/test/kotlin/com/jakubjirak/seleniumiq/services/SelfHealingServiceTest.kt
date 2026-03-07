/*
 * Copyright (c) 2025 Jakub Jirák. All rights reserved.
 * Licensed under the proprietary license. See LICENSE for details.
 */
package com.jakubjirak.seleniumiq.services

import com.jakubjirak.seleniumiq.models.LocatorSuggestion
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.lang.reflect.Constructor
import java.lang.reflect.Method

class SelfHealingServiceTest {

    private fun createService(): SelfHealingService {
        val constructor: Constructor<SelfHealingService> =
            SelfHealingService::class.java.getDeclaredConstructor()
        constructor.isAccessible = true
        return constructor.newInstance()
    }

    private fun isSimilarLocator(service: SelfHealingService, original: String, candidate: String): Boolean {
        val method: Method = SelfHealingService::class.java.getDeclaredMethod(
            "isSimilarLocator", String::class.java, String::class.java
        )
        method.isAccessible = true
        return method.invoke(service, original, candidate) as Boolean
    }

    @Test
    fun `canAutoHeal returns true for id locator`() {
        val service = createService()
        assertTrue(service.canAutoHeal("id"))
    }

    @Test
    fun `canAutoHeal returns true for name locator`() {
        val service = createService()
        assertTrue(service.canAutoHeal("name"))
    }

    @Test
    fun `canAutoHeal returns true for cssSelector`() {
        val service = createService()
        assertTrue(service.canAutoHeal("cssselector"))
    }

    @Test
    fun `canAutoHeal returns false for xpath`() {
        val service = createService()
        assertFalse(service.canAutoHeal("xpath"))
    }

    @Test
    fun `canAutoHeal returns false for unknown type`() {
        val service = createService()
        assertFalse(service.canAutoHeal("tagName"))
    }

    @Test
    fun `isSimilarLocator matches btn keywords`() {
        val service = createService()
        assertTrue(isSimilarLocator(service, "submit-btn", "primary-btn"))
    }

    @Test
    fun `isSimilarLocator matches input keywords`() {
        val service = createService()
        assertTrue(isSimilarLocator(service, "email-input", "user-input"))
    }

    @Test
    fun `isSimilarLocator matches submit keywords`() {
        val service = createService()
        assertTrue(isSimilarLocator(service, "submit-form", "submit-action"))
    }

    @Test
    fun `isSimilarLocator matches similar non-dynamic parts`() {
        val service = createService()
        // Splits on digits, dashes, underscores — "login" should match "login"
        assertTrue(isSimilarLocator(service, "login-123", "login-456"))
    }

    @Test
    fun `isSimilarLocator returns false for unrelated locators`() {
        val service = createService()
        assertFalse(isSimilarLocator(service, "abc", "xyz"))
    }

    @Test
    fun `generateHealingReport contains summary`() {
        val service = createService()
        val originals = listOf(
            LocatorSuggestion("id", "old1", 0.9, "desc", listOf(), LocatorSuggestion.LocatorStrategy.ID),
            LocatorSuggestion("id", "old2", 0.8, "desc", listOf(), LocatorSuggestion.LocatorStrategy.ID)
        )
        val healed = listOf(
            LocatorSuggestion("id", "new1", 0.85, "healed", listOf(), LocatorSuggestion.LocatorStrategy.ID)
        )

        val report = service.generateHealingReport(originals, healed)

        assertTrue(report.contains("Self-Healing Report"))
        assertTrue(report.contains("Original Locators: 2"))
        assertTrue(report.contains("Healed Locators: 1"))
        // Locale-aware: "50.0%" or "50,0%"
        assertTrue(report.contains("50") && report.contains("%"),
            "Report should contain success rate: $report")
        assertTrue(report.contains("Healed #1"))
    }
}
