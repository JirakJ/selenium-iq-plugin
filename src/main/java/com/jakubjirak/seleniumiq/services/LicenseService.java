package com.jakubjirak.seleniumiq.services;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.jakubjirak.seleniumiq.licensing.LicenseTier;

@Service(Service.Level.PROJECT)
public final class LicenseService {
    private final Project project;
    private LicenseTier currentTier = LicenseTier.FREE;
    private String licenseKey = null;
    private long expirationDate = 0;

    public LicenseService(Project project) {
        this.project = project;
    }

    public static LicenseService getInstance(Project project) {
        return project.getService(LicenseService.class);
    }

    public LicenseTier getCurrentTier() {
        return currentTier;
    }

    public boolean hasFeature(String featureName) {
        return isFeatureAvailable(featureName);
    }

    public boolean isFeatureAvailable(String featureName) {
        switch (featureName) {
            case "ai_locator_unlimited":
                return currentTier != LicenseTier.FREE;
            case "self_healing":
                return currentTier.isSelfHealingEnabled();
            case "cross_browser":
                return currentTier.isCrossBrowserEnabled();
            case "advanced_debugging":
                return currentTier.isAdvancedDebugging();
            case "team_features":
                return currentTier.hasTeamFeatures();
            case "visual_regression":
                return currentTier != LicenseTier.FREE;
            case "test_data_generation":
                return currentTier != LicenseTier.FREE;
            case "test_data_management":
                return currentTier == LicenseTier.TEAM || currentTier == LicenseTier.ENTERPRISE;
            case "locator_stability_analyzer":
                return currentTier != LicenseTier.FREE;
            case "page_object_generator":
                return currentTier != LicenseTier.FREE;
            case "professional_reports":
                return currentTier != LicenseTier.FREE;
            case "team_analytics":
                return currentTier == LicenseTier.TEAM || currentTier == LicenseTier.ENTERPRISE;
            case "cicd_integration":
                return currentTier == LicenseTier.TEAM || currentTier == LicenseTier.ENTERPRISE;
            case "compliance_reporting":
                return currentTier == LicenseTier.ENTERPRISE;
            default:
                return true;
        }
    }

    public boolean validateLicense(String key) {
        // TODO: Implement actual license validation with backend API
        if (key != null && key.startsWith("IND-")) {
            currentTier = LicenseTier.INDIVIDUAL;
            licenseKey = key;
            return true;
        } else if (key != null && key.startsWith("TEAM-")) {
            currentTier = LicenseTier.TEAM;
            licenseKey = key;
            return true;
        } else if (key != null && key.startsWith("ENT-")) {
            currentTier = LicenseTier.ENTERPRISE;
            licenseKey = key;
            return true;
        }
        return false;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public boolean isLicenseValid() {
        if (currentTier == LicenseTier.FREE) {
            return true;
        }
        return licenseKey != null && (expirationDate == 0 || System.currentTimeMillis() < expirationDate);
    }

    public void upgradeToTier(LicenseTier tier, String key) {
        this.currentTier = tier;
        this.licenseKey = key;
        this.expirationDate = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000); // 1 year
    }

    public String getUpgradeUrl() {
        return "https://seleniumiq.com/pricing?source=plugin&tier=" + currentTier.name();
    }
}
