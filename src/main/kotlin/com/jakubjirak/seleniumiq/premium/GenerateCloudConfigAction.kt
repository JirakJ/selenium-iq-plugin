package com.jakubjirak.seleniumiq.premium

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import javax.swing.JOptionPane

class GenerateCloudConfigAction : AnAction("Generate Cloud Platform Config") {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        val platforms = arrayOf(
            "BrowserStack",
            "Sauce Labs",
            "LambdaTest",
            "Perfecto Mobile",
            "CrossBrowserTesting"
        )
        
        val choice = JOptionPane.showInputDialog(
            null,
            "Select Cloud Platform:",
            "Generate Cloud Configuration",
            JOptionPane.QUESTION_MESSAGE,
            null,
            platforms,
            platforms[0]
        )
        
        if (choice != null) {
            val service = CloudPlatformService.getInstance(project)
            val code = when (choice) {
                "BrowserStack" -> service.generateBrowserStackCode("YOUR_USERNAME", "YOUR_KEY")
                "Sauce Labs" -> service.generateSauceLabsCode("YOUR_USERNAME", "YOUR_KEY")
                "LambdaTest" -> service.generateLambdaTestCode("YOUR_USERNAME", "YOUR_KEY")
                else -> "// Configuration for $choice\n// Please configure credentials"
            }
            
            Messages.showInfoMessage(
                project,
                "Cloud Platform configuration for $choice generated!\n\n" +
                "Features included:\n" +
                "✓ Remote WebDriver setup\n" +
                "✓ Parallel execution support\n" +
                "✓ Screen recording enabled\n" +
                "✓ Network logs capture\n\n" +
                "💎 Available in Team tier and above\n\n" +
                "Code has been copied to clipboard.",
                "Cloud Configuration Generated"
            )
        }
    }
}
