package com.jakubjirak.seleniumiq.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.jakubjirak.seleniumiq.services.LicenseService;
import com.jakubjirak.seleniumiq.services.UsageTrackingService;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class SeleniumIQConfigurable implements Configurable {
    private final Project project;
    private JPanel mainPanel;
    private JTextField licenseKeyField;
    private JLabel currentTierLabel;
    private JLabel usageLabel;

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

        LicenseService licenseService = LicenseService.getInstance(project);
        UsageTrackingService usageService = UsageTrackingService.getInstance(project);

        // Header
        JLabel headerLabel = new JLabel("SeleniumIQ Configuration");
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 16f));
        mainPanel.add(headerLabel, gbc);

        gbc.gridy++;
        mainPanel.add(new JSeparator(), gbc);

        // Current tier
        gbc.gridy++;
        currentTierLabel = new JLabel("Current Plan: " + licenseService.getCurrentTier().getDisplayName());
        mainPanel.add(currentTierLabel, gbc);

        // Usage stats
        gbc.gridy++;
        int remaining = usageService.getRemainingAILocators();
        String remainingText = remaining == -1 ? "Unlimited" : String.valueOf(remaining);
        usageLabel = new JLabel("AI Locators remaining this month: " + remainingText);
        mainPanel.add(usageLabel, gbc);

        gbc.gridy++;
        JLabel statsLabel = new JLabel(String.format(
            "Total: %d locators | %d self-healing fixes | %d tests analyzed",
            usageService.getTotalLocatorsGenerated(),
            usageService.getTotalSelfHealingFixes(),
            usageService.getTotalTestsAnalyzed()
        ));
        mainPanel.add(statsLabel, gbc);

        gbc.gridy++;
        mainPanel.add(new JSeparator(), gbc);

        // License key
        gbc.gridy++;
        mainPanel.add(new JLabel("License Key:"), gbc);

        gbc.gridy++;
        licenseKeyField = new JTextField(40);
        if (licenseService.getLicenseKey() != null) {
            licenseKeyField.setText(licenseService.getLicenseKey());
        }
        mainPanel.add(licenseKeyField, gbc);

        gbc.gridy++;
        JButton validateButton = new JButton("Validate License");
        validateButton.addActionListener(e -> {
            String key = licenseKeyField.getText().trim();
            if (licenseService.validateLicense(key)) {
                Messages.showInfoMessage(
                    "License activated successfully!\n\nTier: " + licenseService.getCurrentTier().getDisplayName(),
                    "License Activated"
                );
                currentTierLabel.setText("Current Plan: " + licenseService.getCurrentTier().getDisplayName());
            } else {
                Messages.showErrorDialog(
                    "Invalid license key. Please check and try again.",
                    "License Validation Failed"
                );
            }
        });
        mainPanel.add(validateButton, gbc);

        gbc.gridy++;
        JButton upgradeButton = new JButton("Upgrade License");
        upgradeButton.addActionListener(e -> {
            com.intellij.ide.BrowserUtil.browse(licenseService.getUpgradeUrl());
        });
        mainPanel.add(upgradeButton, gbc);

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
