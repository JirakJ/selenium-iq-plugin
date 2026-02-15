package com.jakubjirak.seleniumiq.startup;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.ui.Messages;
import com.jakubjirak.seleniumiq.services.LicenseService;
import com.jakubjirak.seleniumiq.services.UsageTrackingService;
import org.jetbrains.annotations.NotNull;

public class SeleniumIQStartupActivity implements StartupActivity {

    @Override
    public void runActivity(@NotNull Project project) {
        // Welcome dialog disabled to prevent test timeouts and improve user experience
        // Users can access help via Tools menu or plugin documentation
        return;
    }

    private void showWelcomeMessage(Project project, LicenseService licenseService) {
        int result = Messages.showYesNoDialog(
            project,
            "Welcome to SeleniumIQ!\n\n" +
            "Your AI-powered Selenium testing assistant is ready.\n\n" +
            "Key features:\n" +
            "  • AI-powered locator generation (Ctrl+Alt+L)\n" +
            "  • Self-healing locators\n" +
            "  • Flakiness analysis\n" +
            "  • Cross-browser testing\n\n" +
            "Would you like to see a quick tutorial?",
            "Welcome to SeleniumIQ",
            "Show Tutorial",
            "Maybe Later",
            Messages.getInformationIcon()
        );

        if (result == Messages.YES) {
            com.intellij.ide.BrowserUtil.browse("https://seleniumiq.com/docs/quickstart");
        }
    }
}
