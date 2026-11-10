/*
 * Copyright (c) 2025 Jakub Jirák..
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.seleniumiq.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.ui.awt.RelativePoint;
import com.jakubjirak.seleniumiq.models.LocatorSuggestion;
import com.jakubjirak.seleniumiq.models.LocatorSuggestion.LocatorStrategy;
import com.jakubjirak.seleniumiq.util.LocatorParser;
import com.jakubjirak.seleniumiq.util.LocatorParser.ParsedLocator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Heals a broken Selenium locator selected in the editor.
 *
 * <p>The user selects a {@code By.*} expression, triggers this action, and receives a
 * ranked list of stable replacement suggestions derived from static analysis of the
 * original locator. Choosing a suggestion replaces the selection in the editor.
 */
public class HealLocatorsAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) return;

        SelectionModel selection = editor.getSelectionModel();
        String selectedText = selection.getSelectedText();

        if (selectedText == null || selectedText.isBlank()) {
            Messages.showInfoMessage(
                project,
                "Select a locator expression in your code, then run Heal Broken Locators.\n\n" +
                "Example: By.id(\"submit-btn-12345678\")",
                "Select a Locator to Heal"
            );
            return;
        }

        ParsedLocator parsed = LocatorParser.parse(selectedText);
        if (parsed == null) {
            Messages.showWarningDialog(
                project,
                "Could not parse a Selenium By.* expression in the selection.\n\n" +
                "Please select a complete expression such as:\n" +
                "  By.id(\"someId\")\n" +
                "  By.xpath(\"/html/body/div/button\")\n" +
                "  By.className(\"btn\")",
                "Unrecognised Locator"
            );
            return;
        }

        List<LocatorSuggestion> suggestions = buildHealingSuggestions(parsed);

        if (suggestions.isEmpty()) {
            Messages.showInfoMessage(
                project,
                "The selected locator looks stable — no improvements suggested.\n\n" +
                "Locator: " + selectedText,
                "Locator Looks Healthy"
            );
            return;
        }

        showHealingPopup(editor, suggestions, selectedText, project);
    }

    // -------------------------------------------------------------------------
    // Healing logic — static analysis, no live browser required
    // -------------------------------------------------------------------------

    /**
     * Generates healing suggestions by statically analysing the parsed locator.
     * No running browser or page HTML is required.
     */
    private List<LocatorSuggestion> buildHealingSuggestions(ParsedLocator locator) {
        List<LocatorSuggestion> suggestions = new ArrayList<>();

        switch (locator.type()) {
            case "id" -> healId(locator, suggestions);
            case "xpath" -> healXpath(locator, suggestions);
            case "classname" -> healClassName(locator, suggestions);
            case "cssselector" -> healCssSelector(locator, suggestions);
            case "name" -> healName(locator, suggestions);
            default -> { /* tagName / linkText — suggest data-testid convention */ }
        }

        // Universal fallback: data-testid convention recommendation
        if (suggestions.size() < 2) {
            suggestions.add(new LocatorSuggestion(
                "cssSelector",
                "[data-testid='" + semanticName(locator.value()) + "']",
                0.95,
                "Recommended: add data-testid attribute to the element and use this selector",
                List.of("By.cssSelector(\"[data-testid='" + semanticName(locator.value()) + "']\")"),
                LocatorStrategy.CSS_SELECTOR_COMPOUND
            ));
        }

        return suggestions;
    }

    private void healId(ParsedLocator locator, List<LocatorSuggestion> out) {
        if (!LocatorParser.isDynamic(locator)) return; // stable ID — no action needed

        String semantic = semanticName(locator.value());

        // CSS selector using partial class or aria-label derived from the ID stem
        out.add(new LocatorSuggestion(
            "cssSelector",
            "[aria-label='" + semantic + "']",
            0.88,
            "Dynamic ID detected — use aria-label for accessibility + stability",
            List.of("By.cssSelector(\"[aria-label='" + semantic + "']\")"),
            LocatorStrategy.CSS_SELECTOR_COMPOUND
        ));
        out.add(new LocatorSuggestion(
            "cssSelector",
            "[data-testid='" + semantic + "']",
            0.95,
            "Dynamic ID detected — data-testid is the most resilient test attribute",
            List.of("By.cssSelector(\"[data-testid='" + semantic + "']\")"),
            LocatorStrategy.CSS_SELECTOR_COMPOUND
        ));
        out.add(new LocatorSuggestion(
            "cssSelector",
            "[id*='" + semantic + "']",
            0.65,
            "Dynamic ID detected — partial ID match (interim fix)",
            List.of("By.cssSelector(\"[id*='" + semantic + "']\")"),
            LocatorStrategy.CSS_SELECTOR_COMPOUND
        ));
    }

    private void healXpath(ParsedLocator locator, List<LocatorSuggestion> out) {
        if (!LocatorParser.isAbsoluteXPath(locator)) return; // relative XPath — acceptable

        // Convert absolute XPath to relative by taking the deepest meaningful segment
        String[] segments = locator.value().split("/");
        String lastMeaningful = "";
        for (int i = segments.length - 1; i >= 0; i--) {
            if (!segments[i].isBlank() && !segments[i].equalsIgnoreCase("html")
                    && !segments[i].equalsIgnoreCase("body")) {
                lastMeaningful = segments[i];
                break;
            }
        }

        String tag = lastMeaningful.replaceAll("\\[.*]", "");
        if (tag.isBlank()) tag = "element";

        out.add(new LocatorSuggestion(
            "xpath",
            "//" + tag,
            0.55,
            "Absolute XPath is fragile — replaced with relative descendant search",
            List.of("By.xpath(\"" + "//" + tag + "\")"),
            LocatorStrategy.XPATH_RELATIVE
        ));
        out.add(new LocatorSuggestion(
            "cssSelector",
            tag + "[data-testid]",
            0.90,
            "Absolute XPath — switch to CSS with a data-testid attribute on the element",
            List.of("By.cssSelector(\"" + tag + "[data-testid]\")"),
            LocatorStrategy.CSS_SELECTOR_COMPOUND
        ));
    }

    private void healClassName(ParsedLocator locator, List<LocatorSuggestion> out) {
        String cls = locator.value().split("\\s+")[0]; // className supports only one class

        out.add(new LocatorSuggestion(
            "cssSelector",
            "." + cls,
            0.60,
            "Single class name is unstable — at least target with CSS compound selector",
            List.of("By.cssSelector(\"." + cls + "\")"),
            LocatorStrategy.CSS_CLASS
        ));
        out.add(new LocatorSuggestion(
            "cssSelector",
            "[data-testid='" + semanticName(cls) + "']",
            0.95,
            "Single class name is unstable — switch to data-testid",
            List.of("By.cssSelector(\"[data-testid='" + semanticName(cls) + "']\")"),
            LocatorStrategy.CSS_SELECTOR_COMPOUND
        ));
    }

    private void healCssSelector(ParsedLocator locator, List<LocatorSuggestion> out) {
        // Only flag if selector looks unstable (no attribute, just a plain class or tag)
        boolean hasAttribute = locator.value().contains("[");
        boolean hasId = locator.value().contains("#");
        if (hasAttribute || hasId) return;

        out.add(new LocatorSuggestion(
            "cssSelector",
            locator.value() + "[data-testid]",
            0.85,
            "Selector has no stable attribute anchor — add a data-testid filter",
            List.of("By.cssSelector(\"" + locator.value() + "[data-testid]\")"),
            LocatorStrategy.CSS_SELECTOR_COMPOUND
        ));
    }

    private void healName(ParsedLocator locator, List<LocatorSuggestion> out) {
        // name attribute is generally stable; only suggest if value looks dynamic
        if (!LocatorParser.isDynamic(locator)) return;

        out.add(new LocatorSuggestion(
            "cssSelector",
            "[data-testid='" + semanticName(locator.value()) + "']",
            0.95,
            "Dynamic name attribute detected — switch to data-testid",
            List.of("By.cssSelector(\"[data-testid='" + semanticName(locator.value()) + "']\")"),
            LocatorStrategy.CSS_SELECTOR_COMPOUND
        ));
    }

    /** Extracts a human-readable semantic stem from a raw locator value. */
    private static String semanticName(String raw) {
        return raw
            .replaceAll("\\d{4,}", "")   // strip long numeric suffixes
            .replaceAll("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", "")
            .replaceAll("[._]$", "")
            .replaceAll("\\s+", "-")
            .toLowerCase()
            .trim();
    }

    // -------------------------------------------------------------------------
    // UI
    // -------------------------------------------------------------------------

    private void showHealingPopup(Editor editor, List<LocatorSuggestion> suggestions,
                                  String originalText, Project project) {
        BaseListPopupStep<LocatorSuggestion> step = new BaseListPopupStep<>(
            "Healing Suggestions (replaces selection)",
            suggestions
        ) {
            @Override
            public @NotNull String getTextFor(LocatorSuggestion s) {
                return String.format("%-12s %-55s  %s %.0f%%",
                    s.getLocatorType() + ":",
                    s.getLocatorValue(),
                    s.getStabilityLevel(),
                    s.getStabilityScore() * 100
                );
            }

            @Override
            public @Nullable PopupStep<?> onChosen(LocatorSuggestion chosen, boolean finalChoice) {
                if (finalChoice) {
                    String replacement = "By." + chosen.getLocatorType()
                        + "(\"" + chosen.getLocatorValue() + "\")";
                    ApplicationManager.getApplication().runWriteAction(() -> {
                        SelectionModel sel = editor.getSelectionModel();
                        editor.getDocument().replaceString(
                            sel.getSelectionStart(), sel.getSelectionEnd(), replacement
                        );
                    });
                    Messages.showInfoMessage(
                        project,
                        "Locator healed!\n\n" +
                        "Original:    " + originalText + "\n" +
                        "Replacement: " + replacement + "\n\n" +
                        "Stability: " + chosen.getStabilityLevel() +
                        " (" + String.format("%.0f%%", chosen.getStabilityScore() * 100) + ")\n" +
                        "Tip: " + chosen.getDescription(),
                        "Locator Healed"
                    );
                }
                return FINAL_CHOICE;
            }
        };

        JBPopupFactory.getInstance()
            .createListPopup(step)
            .show(new RelativePoint(
                editor.getContentComponent(),
                new Point(editor.getContentComponent().getWidth() / 2, 120)
            ));
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor   = e.getData(CommonDataKeys.EDITOR);
        e.getPresentation().setEnabledAndVisible(project != null && editor != null);
    }
}

