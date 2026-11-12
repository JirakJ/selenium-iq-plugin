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

        // Get HTML snippet from user
        String htmlInput = Messages.showInputDialog(
            project,
            "Paste HTML snippet of the element you want to locate:",
            "Generate Locator",
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

        // Show suggestions in popup
        showLocatorPopup(editor, suggestions, project);
    }

    private void showLocatorPopup(Editor editor, List<LocatorSuggestion> suggestions, 
                                 Project project) {
        BaseListPopupStep<LocatorSuggestion> step = new BaseListPopupStep<>(
            "Locator Suggestions",
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
                        "Locator Inserted"
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
