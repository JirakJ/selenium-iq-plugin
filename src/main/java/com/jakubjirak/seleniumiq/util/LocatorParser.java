/*
 * Copyright (c) 2025 Jakub Jirák. All rights reserved.
 * Licensed under the proprietary license. See LICENSE for details.
 */
package com.jakubjirak.seleniumiq.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses Selenium {@code By.*} locator expressions from source text.
 *
 * <p>Understands all standard {@code By} factory methods:
 * {@code id}, {@code name}, {@code className}, {@code cssSelector},
 * {@code xpath}, {@code tagName}, {@code linkText}, {@code partialLinkText}.
 */
public final class LocatorParser {

    /** Matches {@code By.method("value")} or {@code By.method('value')}. */
    private static final Pattern BY_PATTERN = Pattern.compile(
        "By\\.(id|name|className|cssSelector|xpath|tagName|linkText|partialLinkText)" +
        "\\s*\\(\\s*(?:\"([^\"]*)\"|'([^']*)')\\s*\\)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern ABSOLUTE_XPATH    = Pattern.compile("^/html(/|$)");
    private static final Pattern DYNAMIC_SEGMENT   = Pattern.compile("\\d{5,}|[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}");

    private LocatorParser() {}

    /**
     * Parses the first {@code By.*} expression found in {@code text}.
     *
     * @return parsed result, or {@code null} if no recognisable expression is found
     */
    @Nullable
    public static ParsedLocator parse(@NotNull String text) {
        Matcher m = BY_PATTERN.matcher(text);
        if (m.find()) {
            // group(2) = double-quoted value, group(3) = single-quoted value
            String value = m.group(2) != null ? m.group(2) : m.group(3);
            return new ParsedLocator(m.group(1).toLowerCase(), value);
        }
        return null;
    }

    /** Returns {@code true} when the locator value looks dynamically generated. */
    public static boolean isDynamic(@NotNull ParsedLocator locator) {
        return DYNAMIC_SEGMENT.matcher(locator.value()).find()
            || locator.value().contains("generated")
            || locator.value().contains("random");
    }

    /** Returns {@code true} for absolute XPath expressions starting with {@code /html}. */
    public static boolean isAbsoluteXPath(@NotNull ParsedLocator locator) {
        return "xpath".equals(locator.type()) && ABSOLUTE_XPATH.matcher(locator.value()).find();
    }

    /**
     * Immutable holder for a parsed locator.
     *
     * @param type  lower-cased {@code By} method name, e.g. {@code "id"}, {@code "xpath"}
     * @param value the locator string passed to the {@code By} factory
     */
    public record ParsedLocator(@NotNull String type, @NotNull String value) {}
}
