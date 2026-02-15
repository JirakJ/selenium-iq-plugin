package com.jakubjirak.seleniumiq.services;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.YearMonth;

@Service(Service.Level.PROJECT)
@State(name = "SeleniumIQUsage", storages = @Storage("seleniumIQUsage.xml"))
public final class UsageTrackingService implements PersistentStateComponent<UsageTrackingService.State> {
    
    public static class State {
        public int monthlyAILocatorsUsed = 0;
        public String currentMonth = YearMonth.now().toString();
        public int totalLocatorsGenerated = 0;
        public int totalSelfHealingFixes = 0;
        public int totalTestsAnalyzed = 0;
    }

    private State state = new State();
    private final Project project;

    public UsageTrackingService(Project project) {
        this.project = project;
    }

    public static UsageTrackingService getInstance(Project project) {
        return project.getService(UsageTrackingService.class);
    }

    @Nullable
    @Override
    public State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        XmlSerializerUtil.copyBean(state, this.state);
        checkAndResetMonthlyUsage();
    }

    private void checkAndResetMonthlyUsage() {
        String currentMonth = YearMonth.now().toString();
        if (!currentMonth.equals(state.currentMonth)) {
            state.monthlyAILocatorsUsed = 0;
            state.currentMonth = currentMonth;
        }
    }

    public boolean canUseAILocator() {
        checkAndResetMonthlyUsage();
        LicenseService licenseService = LicenseService.getInstance(project);
        
        if (licenseService.getCurrentTier().isUnlimitedAILocators()) {
            return true;
        }
        
        int limit = licenseService.getCurrentTier().getMonthlyAILocatorLimit();
        return state.monthlyAILocatorsUsed < limit;
    }

    public void recordAILocatorUsage() {
        checkAndResetMonthlyUsage();
        state.monthlyAILocatorsUsed++;
        state.totalLocatorsGenerated++;
    }

    public void recordSelfHealingFix() {
        state.totalSelfHealingFixes++;
    }

    public void recordTestAnalysis() {
        state.totalTestsAnalyzed++;
    }

    public int getMonthlyAILocatorsUsed() {
        checkAndResetMonthlyUsage();
        return state.monthlyAILocatorsUsed;
    }

    public int getRemainingAILocators() {
        LicenseService licenseService = LicenseService.getInstance(project);
        if (licenseService.getCurrentTier().isUnlimitedAILocators()) {
            return -1; // unlimited
        }
        int limit = licenseService.getCurrentTier().getMonthlyAILocatorLimit();
        return Math.max(0, limit - getMonthlyAILocatorsUsed());
    }

    public int getTotalLocatorsGenerated() {
        return state.totalLocatorsGenerated;
    }

    public int getTotalSelfHealingFixes() {
        return state.totalSelfHealingFixes;
    }

    public int getTotalTestsAnalyzed() {
        return state.totalTestsAnalyzed;
    }
}
