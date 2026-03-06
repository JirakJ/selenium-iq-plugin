package com.jakubjirak.seleniumiq.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class SeleniumIQConfigurable implements Configurable {
    private final Project project;
    private JPanel mainPanel;

    public SeleniumIQConfigurable(Project project) {
        this.project = project;
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "SeleniumIQ";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Header
        JLabel headerLabel = new JLabel("SeleniumIQ Configuration");
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 16f));
        mainPanel.add(headerLabel, gbc);

        gbc.gridy++;
        mainPanel.add(new JSeparator(), gbc);

        return mainPanel;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() {
        // Settings are applied immediately in this implementation
    }
}
