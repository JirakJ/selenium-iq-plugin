/*
 * Copyright (c) 2025 Jakub Jirák. All rights reserved.
 * Licensed under the proprietary license. See LICENSE for details.
 */
package com.jakubjirak.seleniumiq.util

import com.jakubjirak.seleniumiq.util.LocatorParser.ParsedLocator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LocatorParserTest {

    // -------------------------------------------------------------------------
    // parse()
    // -------------------------------------------------------------------------

    @Test
    fun `parse returns null for non-By text`() {
        assertNull(LocatorParser.parse("driver.findElement(someMethod())"))
    }

    @Test
    fun `parse extracts id locator`() {
        val result = LocatorParser.parse("""By.id("login-btn")""")
        assertNotNull(result)
        assertEquals("id", result!!.type())
        assertEquals("login-btn", result.value())
    }

    @Test
    fun `parse extracts xpath locator`() {
        val result = LocatorParser.parse("""By.xpath("/html/body/div/button")""")
        assertNotNull(result)
        assertEquals("xpath", result!!.type())
        assertEquals("/html/body/div/button", result.value())
    }

    @Test
    fun `parse extracts cssSelector locator`() {
        val result = LocatorParser.parse("""By.cssSelector("[data-testid='submit']")""")
        assertNotNull(result)
        assertEquals("cssselector", result!!.type())
        assertEquals("[data-testid='submit']", result.value())
    }

    @Test
    fun `parse extracts className locator`() {
        val result = LocatorParser.parse("""By.className("btn-primary")""")
        assertNotNull(result)
        assertEquals("classname", result!!.type())
        assertEquals("btn-primary", result.value())
    }

    @Test
    fun `parse extracts name locator`() {
        val result = LocatorParser.parse("""By.name("username")""")
        assertNotNull(result)
        assertEquals("name", result!!.type())
        assertEquals("username", result.value())
    }

    @Test
    fun `parse handles single-quoted values`() {
        val result = LocatorParser.parse("By.id('submit-btn')")
        assertNotNull(result)
        assertEquals("id", result!!.type())
        assertEquals("submit-btn", result.value())
    }

    @Test
    fun `parse handles whitespace around parentheses`() {
        val result = LocatorParser.parse("""By.id( "login" )""")
        assertNotNull(result)
        assertEquals("login", result!!.value())
    }

    @Test
    fun `parse finds first match in longer code snippet`() {
        val code = """
            driver.findElement(By.id("username")).sendKeys(user);
            driver.findElement(By.id("password")).sendKeys(pass);
        """.trimIndent()
        val result = LocatorParser.parse(code)
        assertNotNull(result)
        assertEquals("username", result!!.value())
    }

    // -------------------------------------------------------------------------
    // isDynamic()
    // -------------------------------------------------------------------------

    @Test
    fun `isDynamic returns true for id with long numeric suffix`() {
        assertTrue(LocatorParser.isDynamic(ParsedLocator("id", "btn-12345678")))
    }

    @Test
    fun `isDynamic returns true for UUID-style id`() {
        assertTrue(LocatorParser.isDynamic(ParsedLocator("id", "a1b2c3d4-e5f6-7890-ab12-cd34ef567890")))
    }

    @Test
    fun `isDynamic returns false for stable id`() {
        assertFalse(LocatorParser.isDynamic(ParsedLocator("id", "login-btn")))
    }

    @Test
    fun `isDynamic returns true when value contains generated`() {
        assertTrue(LocatorParser.isDynamic(ParsedLocator("id", "generated-id")))
    }

    // -------------------------------------------------------------------------
    // isAbsoluteXPath()
    // -------------------------------------------------------------------------

    @Test
    fun `isAbsoluteXPath returns true for path starting with html`() {
        assertTrue(LocatorParser.isAbsoluteXPath(ParsedLocator("xpath", "/html/body/div/button")))
    }

    @Test
    fun `isAbsoluteXPath returns false for relative xpath`() {
        assertFalse(LocatorParser.isAbsoluteXPath(ParsedLocator("xpath", "//button[@id='ok']")))
    }

    @Test
    fun `isAbsoluteXPath returns false for non-xpath type`() {
        assertFalse(LocatorParser.isAbsoluteXPath(ParsedLocator("id", "/html/body")))
    }
}
