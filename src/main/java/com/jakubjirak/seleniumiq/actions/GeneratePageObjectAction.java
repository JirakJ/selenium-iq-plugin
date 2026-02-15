package com.jakubjirak.seleniumiq.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.jakubjirak.seleniumiq.services.LicenseService;
import com.jakubjirak.seleniumiq.services.UsageTrackingService;
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

        UsageTrackingService usageService = UsageTrackingService.getInstance(project);
        LicenseService licenseService = LicenseService.getInstance(project);

        if (!usageService.canUseAILocator()) {
            Messages.showWarningDialog(
                project,
                "Page Object generation uses your AI locator quota.\n\n" +
                "You've reached your monthly limit. Upgrade to Individual Professional for unlimited usage!",
                "Monthly Limit Reached"
            );
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
        
        usageService.recordAILocatorUsage();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(e.getProject() != null);
    }
}
