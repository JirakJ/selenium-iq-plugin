package com.jakubjirak.seleniumiq.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.jakubjirak.seleniumiq.licensing.LicenseTier;
import com.jakubjirak.seleniumiq.services.LicenseService;
import org.jetbrains.annotations.NotNull;

public class UpgradeLicenseAction extends AnAction {

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

        LicenseService licenseService = LicenseService.getInstance(project);
        LicenseTier currentTier = licenseService.getCurrentTier();

        String message = buildUpgradeMessage(currentTier);
        
        int result = Messages.showYesNoDialog(
            project,
            message,
            "Upgrade SeleniumIQ License",
            "View Pricing",
            "Maybe Later",
            Messages.getInformationIcon()
        );

        if (result == Messages.YES) {
            com.intellij.ide.BrowserUtil.browse(licenseService.getUpgradeUrl());
        }
    }

    private String buildUpgradeMessage(LicenseTier currentTier) {
        StringBuilder msg = new StringBuilder();
        msg.append("Current Plan: ").append(currentTier.getDisplayName()).append("\n\n");
        
        if (currentTier == LicenseTier.FREE) {
            msg.append("Upgrade to unlock powerful features:\n\n");
            msg.append("Individual Professional ($19/month):\n");
            msg.append("  ✓ Unlimited AI locators\n");
            msg.append("  ✓ Self-healing locators\n");
            msg.append("  ✓ Cross-browser testing\n");
            msg.append("  ✓ Advanced debugging\n");
            msg.append("  ✓ Flakiness prediction\n\n");
            msg.append("Team ($99/month for 5-10 users):\n");
            msg.append("  ✓ All Individual features\n");
            msg.append("  ✓ Shared test repository\n");
            msg.append("  ✓ Team analytics\n");
            msg.append("  ✓ CI/CD integrations\n\n");
            msg.append("Enterprise (Custom pricing):\n");
            msg.append("  ✓ All Team features\n");
            msg.append("  ✓ SSO & RBAC\n");
            msg.append("  ✓ Compliance reporting\n");
            msg.append("  ✓ Dedicated support\n");
        } else if (currentTier == LicenseTier.INDIVIDUAL) {
            msg.append("Upgrade to Team or Enterprise for:\n\n");
            msg.append("  ✓ Collaborative features\n");
            msg.append("  ✓ Team analytics dashboard\n");
            msg.append("  ✓ Shared test assets\n");
            msg.append("  ✓ Advanced integrations\n");
            msg.append("  ✓ Priority support\n");
        } else {
            msg.append("You're on the ").append(currentTier.getDisplayName()).append(" plan.\n\n");
            msg.append("Contact us for custom enterprise features or volume discounts.");
        }
        
        return msg.toString();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(e.getProject() != null);
    }
}
