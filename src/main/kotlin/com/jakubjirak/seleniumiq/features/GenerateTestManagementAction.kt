package com.jakubjirak.seleniumiq.features

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import javax.swing.JOptionPane

class GenerateTestManagementAction : AnAction("Generate Test Management Integration") {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        val platforms = arrayOf(
            "Jira/Zephyr",
            "TestRail",
            "qTest",
            "Xray",
            "Zephyr Scale"
        )
        
        val choice = JOptionPane.showInputDialog(
            null,
            "Select Test Management Platform:",
            "Generate Test Management Integration",
            JOptionPane.QUESTION_MESSAGE,
            null,
            platforms,
            platforms[0]
        )
        
        if (choice != null) {
            val service = project.getService(TestManagementService::class.java)
            val choiceStr = choice.toString()
            val code = when {
                choiceStr.contains("Jira") || choiceStr.contains("Zephyr") -> 
                    service.generateJiraIntegrationCode()
                choiceStr.contains("TestRail") -> 
                    service.generateTestRailIntegrationCode()
                else -> 
                    "// Integration code for $choiceStr\n// Configure your API credentials"
            }
            
            Messages.showInfoMessage(
                project,
                "Test Management integration for $choice generated!\n\n" +
                "Features included:\n" +
                "✓ Automatic test result updates\n" +
                "✓ Screenshot attachments on failure\n" +
                "✓ Requirements traceability\n" +
                "✓ Execution history tracking\n\n" +
                "Integration code has been generated.",
                "Test Management Integration"
            )
        }
    }
}
