package com.jakubjirak.seleniumiq.premium

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.jakubjirak.seleniumiq.licensing.LicenseManager

class PerformanceMonitorAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        if (!LicenseManager.getInstance().hasFeature("performance_monitoring")) {
            Messages.showInfoMessage(
                project,
                "Performance monitoring is available in Team and Enterprise tiers.\n\nUpgrade to unlock this feature.",
                "Premium Feature"
            )
            return
        }
        
        Messages.showInfoMessage(
            project,
            "Performance Monitoring\n\n" +
                    "• Test execution timing\n" +
                    "• Locator performance analysis\n" +
                    "• Memory usage tracking\n" +
                    "• Performance regression detection",
            "Performance Monitoring"
        )
    }
    
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }
}
