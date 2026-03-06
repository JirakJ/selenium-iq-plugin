package com.jakubjirak.seleniumiq.startup;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class SeleniumIQStartupActivity implements StartupActivity {

    @Override
    public void runActivity(@NotNull Project project) {
        // Welcome dialog disabled to prevent test timeouts and improve user experience
        // Users can access help via Tools menu or plugin documentation
        return;
    }
}
