package com.jakubjirak.seleniumiq.enterprise;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.jakubjirak.seleniumiq.licensing.LicenseManager;
import com.jakubjirak.seleniumiq.licensing.LicenseTier;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SSOIntegration {
    private final Project project;
    private final LicenseManager licenseManager;

    public enum SSOProvider {
        SAML("SAML 2.0", "Enterprise SSO via SAML"),
        OAUTH("OAuth 2.0", "Modern OAuth authentication"),
        LDAP("LDAP", "Active Directory integration"),
        OKTA("Okta", "Okta identity platform"),
        AZURE_AD("Azure AD", "Microsoft Azure Active Directory");

        private final String displayName;
        private final String description;

        SSOProvider(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    public SSOIntegration(@NotNull Project project) {
        this.project = project;
        this.licenseManager = LicenseManager.getInstance(project);
    }

    public boolean configureSSOProvider(SSOProvider provider, Map<String, String> config) {
        if (!isEnterpriseFeature()) {
            showEnterpriseRequired();
            return false;
        }
        return true;
    }

    public String generateAPIToken(String userId, Set<String> scopes, int expirationDays) {
        if (!isEnterpriseFeature()) {
            return "";
        }
        return "sqiq_" + UUID.randomUUID().toString().replace("-", "");
    }

    private boolean isEnterpriseFeature() {
        LicenseTier tier = licenseManager.getCurrentTier();
        return tier == LicenseTier.ENTERPRISE;
    }

    private void showEnterpriseRequired() {
        Messages.showInfoMessage(
            project,
            "SSO Integration is available in Enterprise plan only.\n\n" +
            "Upgrade to unlock:\n" +
            "• SAML 2.0, OAuth 2.0, LDAP integration\n" +
            "• Okta, Azure AD support\n\n" +
            "Enterprise plan: Starting at $499/month",
            "Enterprise Feature Required"
        );
    }
}
