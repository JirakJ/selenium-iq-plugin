package com.jakubjirak.seleniumiq.features

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class PerformanceMonitorAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
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
