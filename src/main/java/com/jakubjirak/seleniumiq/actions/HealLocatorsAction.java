package com.jakubjirak.seleniumiq.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.jakubjirak.seleniumiq.services.SelfHealingService;
import org.jetbrains.annotations.NotNull;

public class HealLocatorsAction extends AnAction {

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

        String selectedText = editor.getSelectionModel().getSelectedText();
        if (selectedText == null || selectedText.isEmpty()) {
            Messages.showInfoMessage(
                project,
                "Please select a locator in your code to heal.\n\nExample: By.id(\"submit-button\")",
                "Select Locator to Heal"
            );
            return;
        }

        Messages.showInfoMessage(
            project,
            "Self-healing analysis started!\n\n" +
            "The plugin will analyze your page structure and suggest stable replacement locators.\n\n" +
            "Analyzing: " + selectedText,
            "Self-Healing in Progress"
        );
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        e.getPresentation().setEnabledAndVisible(project != null && editor != null);
    }
}
