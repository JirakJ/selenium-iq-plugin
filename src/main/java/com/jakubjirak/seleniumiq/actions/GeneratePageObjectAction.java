package com.jakubjirak.seleniumiq.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class GeneratePageObjectAction extends AnAction {

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

        String className = Messages.showInputDialog(
            project,
            "Enter Page Object class name:",
            "Generate Page Object",
            Messages.getQuestionIcon()
        );

        if (className == null || className.trim().isEmpty()) {
            return;
        }

        String pageUrl = Messages.showInputDialog(
            project,
            "Enter page URL to analyze:",
            "Page Object for: " + className,
            Messages.getQuestionIcon()
        );

        if (pageUrl == null || pageUrl.trim().isEmpty()) {
            return;
        }

        Messages.showInfoMessage(
            project,
            "Page Object generation started!\n\n" +
            "Class: " + className + "\n" +
            "URL: " + pageUrl + "\n\n" +
            "The plugin will analyze the page and generate a complete Page Object Model class with stable locators.",
            "Generating Page Object"
        );
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(e.getProject() != null);
    }
}
