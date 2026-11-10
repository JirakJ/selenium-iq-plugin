/*
 * Copyright (c) 2025 Jakub Jirák..
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.seleniumiq.services

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.lang.reflect.Constructor

class AILocatorServiceTest {

    private fun createService(): AILocatorService {
        val constructor: Constructor<AILocatorService> =
            AILocatorService::class.java.getDeclaredConstructor()
        constructor.isAccessible = true
        return constructor.newInstance()
    }

    @Test
    fun `generateLocators returns ID-based locator for element with stable id`() {
        val service = createService()
        val html = """<div><button id="loginBtn">Login</button></div>"""
        val suggestions = service.generateLocators(html, "button")

        assertTrue(suggestions.any { it.locatorType == "id" && it.locatorValue == "loginBtn" })
        val idSuggestion = suggestions.first { it.locatorType == "id" }
        assertEquals(0.95, idSuggestion.stabilityScore, 0.001)
    }

    @Test
    fun `generateLocators skips dynamic IDs`() {
        val service = createService()
        val html = """<div><button id="btn-123456789">Click</button></div>"""
        val suggestions = service.generateLocators(html, "button")

        assertFalse(suggestions.any { it.locatorType == "id" && it.locatorValue == "btn-123456789" })
    }

    @Test
    fun `generateLocators returns name-based locator`() {
        val service = createService()
        val html = """<input name="username" type="text"/>"""
        val suggestions = service.generateLocators(html, "input")

        assertTrue(suggestions.any { it.locatorType == "name" && it.locatorValue == "username" })
        val nameSuggestion = suggestions.first { it.locatorType == "name" }
        assertEquals(0.85, nameSuggestion.stabilityScore, 0.001)
    }

    @Test
    fun `generateLocators returns data-testid locator`() {
        val service = createService()
        val html = """<button data-testid="submit-btn">Submit</button>"""
        val suggestions = service.generateLocators(html, "button")

        assertTrue(suggestions.any { it.locatorValue.contains("data-testid") })
        val testIdSuggestion = suggestions.first { it.locatorValue.contains("data-testid") }
        assertEquals(0.90, testIdSuggestion.stabilityScore, 0.001)
    }

    @Test
    fun `generateLocators builds compound CSS selector`() {
        val service = createService()
        val html = """<input class="form-control" type="email" placeholder="Enter email"/>"""
        val suggestions = service.generateLocators(html, "input")

        assertTrue(suggestions.any { it.locatorType == "cssSelector" && it.locatorValue.contains("form-control") })
    }

    @Test
    fun `generateLocators creates text-based XPath for short text`() {
        val service = createService()
        val html = """<button>Submit Form</button>"""
        val suggestions = service.generateLocators(html, "button")

        assertTrue(suggestions.any {
            it.locatorType == "xpath" && it.locatorValue.contains("Submit Form")
        })
    }

    @Test
    fun `generateLocators always includes relative XPath`() {
        val service = createService()
        val html = """<span>Hello</span>"""
        val suggestions = service.generateLocators(html, "span")

        assertTrue(suggestions.any {
            it.locatorType == "xpath" && it.locatorValue.startsWith("//span")
        })
    }

    @Test
    fun `generateLocators returns empty list for non-existent selector`() {
        val service = createService()
        val html = """<div><p>Hello</p></div>"""
        val suggestions = service.generateLocators(html, "button")

        assertTrue(suggestions.isEmpty())
    }

    @Test
    fun `calculateStabilityScore returns high for stable id`() {
        val service = createService()
        assertEquals(0.95, service.calculateStabilityScore("login-btn", "id"), 0.001)
    }

    @Test
    fun `calculateStabilityScore returns low for dynamic id`() {
        val service = createService()
        assertEquals(0.4, service.calculateStabilityScore("element-123456", "id"), 0.001)
    }

    @Test
    fun `calculateStabilityScore returns 0_85 for name`() {
        val service = createService()
        assertEquals(0.85, service.calculateStabilityScore("username", "name"), 0.001)
    }

    @Test
    fun `calculateStabilityScore returns 0_2 for absolute xpath`() {
        val service = createService()
        assertEquals(0.2, service.calculateStabilityScore("//html/body/div/p", "xpath"), 0.001)
    }

    @Test
    fun `calculateStabilityScore returns 0_7 for text-based xpath`() {
        val service = createService()
        assertEquals(0.7, service.calculateStabilityScore("//button[text()='Login']", "xpath"), 0.001)
    }

    @Test
    fun `calculateStabilityScore returns 0_9 for data-testid cssSelector`() {
        val service = createService()
        assertEquals(0.9, service.calculateStabilityScore("[data-testid='btn']", "cssselector"), 0.001)
    }

    @Test
    fun `calculateStabilityScore returns 0_75 for compound cssSelector`() {
        val service = createService()
        assertEquals(0.75, service.calculateStabilityScore("button.primary[type='submit']", "cssselector"), 0.001)
    }

    @Test
    fun `calculateStabilityScore returns 0_5 for unknown type`() {
        val service = createService()
        assertEquals(0.5, service.calculateStabilityScore("anything", "unknown"), 0.001)
    }

    @Test
    fun `generateLocators handles invalid HTML gracefully`() {
        val service = createService()
        val suggestions = service.generateLocators("<<not valid>>html!!", "div")
        // Should not throw, may return empty or partial results
        assertNotNull(suggestions)
    }

    @Test
    fun `generateLocators skips dynamic class names in compound selector`() {
        val service = createService()
        val html = """<button class="btn generated-style12345">Click</button>"""
        val suggestions = service.generateLocators(html, "button")

        val compoundSuggestions = suggestions.filter {
            it.locatorType == "cssSelector" && it.locatorValue.contains(".")
        }
        // Should not include 'generated-style12345' (contains 'generated')
        compoundSuggestions.forEach { suggestion ->
            assertFalse(suggestion.locatorValue.contains("generated-style12345"))
        }
    }
}
