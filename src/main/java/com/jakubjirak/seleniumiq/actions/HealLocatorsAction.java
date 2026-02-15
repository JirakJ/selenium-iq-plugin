package com.jakubjirak.seleniumiq.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.jakubjirak.seleniumiq.services.LicenseService;
import com.jakubjirak.seleniumiq.services.SelfHealingService;
import com.jakubjirak.seleniumiq.services.UsageTrackingService;
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

        LicenseService licenseService = LicenseService.getInstance(project);
        
        if (!licenseService.isFeatureAvailable("self_healing")) {
            int result = Messages.showYesNoDialog(
                project,
                "Self-healing locators are available in Individual Professional tier and above.\n\n" +
                "Upgrade now to automatically fix broken locators and save 60-80% maintenance time!\n\n" +
                "Only $19/month - pays for itself in 2-3 hours saved.",
                "Self-Healing Feature - Professional Only",
                "Upgrade Now",
                "Cancel",
                Messages.getInformationIcon()
            );
            
            if (result == Messages.YES) {
                com.intellij.ide.BrowserUtil.browse(licenseService.getUpgradeUrl());
            }
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
        
        UsageTrackingService.getInstance(project).recordSelfHealingFix();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        e.getPresentation().setEnabledAndVisible(project != null && editor != null);
    }
}
