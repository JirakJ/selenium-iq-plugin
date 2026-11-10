/*
 * Copyright (c) 2025 Jakub Jirák..
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.seleniumiq.models

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LocatorSuggestionTest {

    @Test
    fun `getSeleniumCode formats correctly for id locator`() {
        val suggestion = LocatorSuggestion(
            "id", "loginBtn", 0.95, "desc",
            listOf(), LocatorSuggestion.LocatorStrategy.ID
        )

        assertEquals("driver.findElement(By.id(\"loginBtn\"))", suggestion.seleniumCode)
    }

    @Test
    fun `getSeleniumCode formats correctly for cssSelector`() {
        val suggestion = LocatorSuggestion(
            "cssSelector", "[data-testid='submit']", 0.9, "desc",
            listOf(), LocatorSuggestion.LocatorStrategy.CSS_SELECTOR_COMPOUND
        )

        assertEquals(
            "driver.findElement(By.cssSelector(\"[data-testid='submit']\"))",
            suggestion.seleniumCode
        )
    }

    @Test
    fun `getStabilityLevel returns Excellent for score above 0_8`() {
        val suggestion = createSuggestion(0.95)
        assertEquals("Excellent", suggestion.stabilityLevel)
    }

    @Test
    fun `getStabilityLevel returns Good for score 0_6 to 0_8`() {
        val suggestion = createSuggestion(0.65)
        assertEquals("Good", suggestion.stabilityLevel)
    }

    @Test
    fun `getStabilityLevel returns Moderate for score 0_4 to 0_6`() {
        val suggestion = createSuggestion(0.45)
        assertEquals("Moderate", suggestion.stabilityLevel)
    }

    @Test
    fun `getStabilityLevel returns Poor for score below 0_4`() {
        val suggestion = createSuggestion(0.2)
        assertEquals("Poor", suggestion.stabilityLevel)
    }

    @Test
    fun `LocatorStrategy enum has correct selenium methods`() {
        assertEquals("id", LocatorSuggestion.LocatorStrategy.ID.seleniumMethod)
        assertEquals("name", LocatorSuggestion.LocatorStrategy.NAME.seleniumMethod)
        assertEquals("cssSelector", LocatorSuggestion.LocatorStrategy.CSS_CLASS.seleniumMethod)
        assertEquals("linkText", LocatorSuggestion.LocatorStrategy.LINK_TEXT.seleniumMethod)
        assertEquals("tagName", LocatorSuggestion.LocatorStrategy.TAG_NAME.seleniumMethod)
        assertEquals("xpath", LocatorSuggestion.LocatorStrategy.XPATH_ABSOLUTE.seleniumMethod)
        assertEquals("xpath", LocatorSuggestion.LocatorStrategy.XPATH_RELATIVE.seleniumMethod)
        assertEquals("xpath", LocatorSuggestion.LocatorStrategy.XPATH_TEXT.seleniumMethod)
    }

    @Test
    fun `LocatorStrategy enum has 10 values`() {
        assertEquals(10, LocatorSuggestion.LocatorStrategy.values().size)
    }

    @Test
    fun `fallbackLocators are stored correctly`() {
        val fallbacks = listOf("By.id(\"alt1\")", "By.cssSelector(\".alt2\")")
        val suggestion = LocatorSuggestion(
            "id", "main", 0.9, "desc",
            fallbacks, LocatorSuggestion.LocatorStrategy.ID
        )

        assertEquals(2, suggestion.fallbackLocators.size)
        assertEquals("By.id(\"alt1\")", suggestion.fallbackLocators[0])
    }

    private fun createSuggestion(stabilityScore: Double): LocatorSuggestion {
        return LocatorSuggestion(
            "id", "test", stabilityScore, "desc",
            listOf(), LocatorSuggestion.LocatorStrategy.ID
        )
    }
}
