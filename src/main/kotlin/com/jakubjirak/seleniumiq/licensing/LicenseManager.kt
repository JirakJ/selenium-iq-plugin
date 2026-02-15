package com.jakubjirak.seleniumiq.licensing

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

enum class LicenseTier(
    @get:JvmName("getDisplayName") val displayName: String,
    @get:JvmName("getMonthlyPrice") val monthlyPrice: Int,
    @get:JvmName("getMonthlyAILocatorLimit") val monthlyAILocatorLimit: Int,
    @get:JvmName("isSelfHealingEnabled") val selfHealingEnabled: Boolean,
    @get:JvmName("isCrossBrowserEnabled") val crossBrowserEnabled: Boolean,
    @get:JvmName("isAdvancedDebugging") val advancedDebugging: Boolean,
    @get:JvmName("hasTeamFeatures") val teamFeatures: Boolean
) {
    FREE("Free", 0, 100, false, false, false, false),
    INDIVIDUAL("Individual Professional", 19, -1, true, true, true, false),
    TEAM("Team", 99, -1, true, true, true, true),
    ENTERPRISE("Enterprise", 499, -1, true, true, true, true);
    
    fun isUnlimitedAILocators() = monthlyAILocatorLimit == -1
}

@Service
class LicenseManager {
    private var currentTier: LicenseTier = LicenseTier.FREE
    private val featureMap = mapOf(
        LicenseTier.FREE to setOf(
            "syntax_highlighting",
            "basic_locator_generation",
            "element_inspector",
            "project_templates",
            "basic_code_completion"
        ),
        LicenseTier.INDIVIDUAL to setOf(
            "syntax_highlighting",
            "basic_locator_generation",
            "element_inspector",
            "project_templates",
            "basic_code_completion",
            "ai_locator_generation",
            "self_healing_locators",
            "cross_browser_testing",
            "intelligent_wait_strategies",
            "flakiness_prediction",
            "advanced_debugging",
            "performance_monitoring",
            "test_recorder",
            "api_test_generation"
        ),
        LicenseTier.TEAM to setOf(
            "syntax_highlighting",
            "basic_locator_generation",
            "element_inspector",
            "project_templates",
            "basic_code_completion",
            "ai_locator_generation",
            "self_healing_locators",
            "cross_browser_testing",
            "intelligent_wait_strategies",
            "flakiness_prediction",
            "advanced_debugging",
            "performance_monitoring",
            "test_recorder",
            "api_test_generation",
            "shared_test_repository",
            "team_analytics",
            "collaborative_review",
            "standardization_enforcement",
            "ci_cd_integration",
            "test_management_integration",
            "data_driven_testing"
        ),
        LicenseTier.ENTERPRISE to setOf(
            "syntax_highlighting",
            "basic_locator_generation",
            "element_inspector",
            "project_templates",
            "basic_code_completion",
            "ai_locator_generation",
            "self_healing_locators",
            "cross_browser_testing",
            "intelligent_wait_strategies",
            "flakiness_prediction",
            "advanced_debugging",
            "performance_monitoring",
            "test_recorder",
            "api_test_generation",
            "shared_test_repository",
            "team_analytics",
            "collaborative_review",
            "standardization_enforcement",
            "ci_cd_integration",
            "test_management_integration",
            "data_driven_testing",
            "audit_trails",
            "compliance_reporting",
            "sso_integration",
            "rbac",
            "custom_integrations",
            "company_specific_ai_training",
            "on_premise_deployment"
        )
    )
    
    fun getCurrentTier(): LicenseTier = currentTier
    
    fun setLicenseTier(tier: LicenseTier) {
        currentTier = tier
    }
    
    fun hasFeature(feature: String): Boolean {
        return featureMap[currentTier]?.contains(feature) ?: false
    }
    
    fun getAvailableFeatures(): Set<String> {
        return featureMap[currentTier] ?: emptySet()
    }
    
    fun isFeatureLocked(feature: String): Boolean {
        return !hasFeature(feature)
    }
    
    fun getRequiredTierForFeature(feature: String): LicenseTier? {
        return featureMap.entries.firstOrNull { it.value.contains(feature) }?.key
    }
    
    companion object {
        @JvmStatic
        fun getInstance(project: Project): LicenseManager {
            return ApplicationManager.getApplication().getService(LicenseManager::class.java)
        }
        
        @JvmStatic
        fun getInstance(): LicenseManager {
            return ApplicationManager.getApplication().getService(LicenseManager::class.java)
        }
    }
}
