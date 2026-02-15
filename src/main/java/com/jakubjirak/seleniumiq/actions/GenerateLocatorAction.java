package com.jakubjirak.seleniumiq.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.ui.awt.RelativePoint;
import com.jakubjirak.seleniumiq.models.LocatorSuggestion;
import com.jakubjirak.seleniumiq.services.AILocatorService;
import com.jakubjirak.seleniumiq.services.LicenseService;
import com.jakubjirak.seleniumiq.services.UsageTrackingService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class GenerateLocatorAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return;
        }

        UsageTrackingService usageService = UsageTrackingService.getInstance(project);
        LicenseService licenseService = LicenseService.getInstance(project);

        // Check usage limits
        if (!usageService.canUseAILocator()) {
            int remaining = usageService.getRemainingAILocators();
            Messages.showWarningDialog(
                project,
                "You've reached your monthly limit of AI locators (" + 
                licenseService.getCurrentTier().getMonthlyAILocatorLimit() + ").\n\n" +
                "Upgrade to Individual Professional for unlimited AI locators!\n" +
                "Only $19/month - saves 10-15 hours of work monthly.",
                "AI Locator Limit Reached"
            );
            
            // Show upgrade option
            int result = Messages.showYesNoDialog(
                project,
                "Would you like to upgrade to Individual Professional tier?",
                "Upgrade to Professional",
                "Upgrade Now",
                "Maybe Later",
                Messages.getQuestionIcon()
            );
            
            if (result == Messages.YES) {
                // Open upgrade URL
                com.intellij.ide.BrowserUtil.browse(licenseService.getUpgradeUrl());
            }
            return;
        }

        // Get HTML snippet from user
        String htmlInput = Messages.showInputDialog(
            project,
            "Paste HTML snippet of the element you want to locate:",
            "Generate AI Locator",
            Messages.getQuestionIcon()
        );

        if (htmlInput == null || htmlInput.trim().isEmpty()) {
            return;
        }

        // Generate locators
        AILocatorService aiService = AILocatorService.getInstance();
        List<LocatorSuggestion> suggestions = aiService.generateLocators(htmlInput, "body > *");

        if (suggestions.isEmpty()) {
            Messages.showWarningDialog(
                project,
                "No stable locators could be generated. Please provide valid HTML.",
                "No Locators Found"
            );
            return;
        }

        // Record usage
        usageService.recordAILocatorUsage();
        
        int remaining = usageService.getRemainingAILocators();
        String remainingText = remaining == -1 ? "Unlimited" : String.valueOf(remaining);

        // Show suggestions in popup
        showLocatorPopup(editor, suggestions, project, remainingText);
    }

    private void showLocatorPopup(Editor editor, List<LocatorSuggestion> suggestions, 
                                 Project project, String remainingLocators) {
        BaseListPopupStep<LocatorSuggestion> step = new BaseListPopupStep<>(
            "AI Locator Suggestions (Remaining this month: " + remainingLocators + ")",
            suggestions
        ) {
            @NotNull
            @Override
            public String getTextFor(LocatorSuggestion value) {
                return String.format(
                    "%s: %s [%s - %.0f%%]",
                    value.getLocatorType(),
                    value.getLocatorValue(),
                    value.getStabilityLevel(),
                    value.getStabilityScore() * 100
                );
            }

            @Nullable
            @Override
            public PopupStep<?> onChosen(LocatorSuggestion selectedValue, boolean finalChoice) {
                if (finalChoice) {
                    // Insert locator into editor
                    String code = selectedValue.getSeleniumCode();
                    
                    com.intellij.openapi.application.ApplicationManager.getApplication().runWriteAction(() -> {
                        int offset = editor.getCaretModel().getOffset();
                        editor.getDocument().insertString(offset, code);
                    });
                    
                    // Show details
                    Messages.showInfoMessage(
                        project,
                        "Locator inserted!\n\n" +
                        "Stability: " + selectedValue.getStabilityLevel() + 
                        " (" + String.format("%.0f%%", selectedValue.getStabilityScore() * 100) + ")\n" +
                        "Strategy: " + selectedValue.getStrategy().getDescription(),
                        "AI Locator Generated"
                    );
                }
                return FINAL_CHOICE;
            }
        };

        JBPopupFactory.getInstance()
            .createListPopup(step)
            .show(new RelativePoint(editor.getContentComponent(), 
                new Point(editor.getContentComponent().getWidth() / 2, 100)));
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        e.getPresentation().setEnabledAndVisible(project != null && editor != null);
    }
}
