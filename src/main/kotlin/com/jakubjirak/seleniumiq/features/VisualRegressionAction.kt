package com.jakubjirak.seleniumiq.features

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class VisualRegressionAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        Messages.showInfoMessage(
            project,
            "Visual Regression Testing\n\n" +
                    "• Automated screenshot comparison\n" +
                    "• Pixel-perfect diff analysis\n" +
                    "• Integration with Percy, Applitools\n" +
                    "• Baseline management",
            "Visual Regression Testing"
        )
    }
    
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }
}
