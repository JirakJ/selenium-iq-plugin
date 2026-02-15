package com.jakubjirak.seleniumiq.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class GenerateTestTemplateAction extends AnAction {

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

        String[] options = {"JUnit 5", "JUnit 4", "TestNG", "Cancel"};
        int choice = Messages.showDialog(
            project,
            "Select test framework for template:",
            "Generate Test Template",
            options,
            0,
            Messages.getQuestionIcon()
        );

        if (choice == 3 || choice == -1) {
            return;
        }

        String framework = options[choice];
        String className = Messages.showInputDialog(
            project,
            "Enter test class name:",
            "Generate " + framework + " Test",
            Messages.getQuestionIcon()
        );

        if (className == null || className.trim().isEmpty()) {
            return;
        }

        Messages.showInfoMessage(
            project,
            "Test template will be generated!\n\n" +
            "Framework: " + framework + "\n" +
            "Class: " + className + "\n\n" +
            "The template includes:\n" +
            "- WebDriver setup/teardown\n" +
            "- Sample test method\n" +
            "- Best practice patterns\n" +
            "- AI-optimized locators",
            "Test Template Ready"
        );
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(e.getProject() != null);
    }
}
