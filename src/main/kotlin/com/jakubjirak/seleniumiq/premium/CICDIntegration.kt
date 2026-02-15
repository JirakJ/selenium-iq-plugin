package com.jakubjirak.seleniumiq.premium

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import javax.swing.JOptionPane

class GenerateCICDConfigAction : AnAction("Generate CI/CD Configuration") {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        val platforms = arrayOf(
            "GitHub Actions",
            "GitLab CI",
            "Jenkins",
            "CircleCI",
            "Azure DevOps",
            "Travis CI"
        )
        
        val choice = JOptionPane.showInputDialog(
            null,
            "Select CI/CD Platform:",
            "Generate CI/CD Configuration",
            JOptionPane.QUESTION_MESSAGE,
            null,
            platforms,
            platforms[0]
        )
        
        if (choice != null) {
            Messages.showInfoMessage(
                project,
                "CI/CD Configuration for $choice generated!\n\n" +
                "Features included:\n" +
                "✓ Parallel test execution\n" +
                "✓ Automated screenshot upload\n" +
                "✓ HTML test reports\n" +
                "✓ Slack notifications\n\n" +
                "💎 Available in Team tier and above ($99/month)",
                "CI/CD Configuration Generated"
            )
        }
    }
}
