package com.jakubjirak.seleniumiq.features

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.*

class TeamAnalyticsDashboard : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val content = ContentFactory.getInstance().createContent(
            createDashboardPanel(),
            "",
            false
        )
        toolWindow.contentManager.addContent(content)
    }

    private fun createDashboardPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        
        val header = JLabel("<html><h2>📊 Team Analytics Dashboard</h2></html>")
        panel.add(header, BorderLayout.NORTH)
        
        val metricsPanel = JPanel(GridLayout(2, 3, 10, 10))
        metricsPanel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        
        metricsPanel.add(createMetricCard("Total Tests", "1,247", "↑ 12%"))
        metricsPanel.add(createMetricCard("Pass Rate", "94.2%", "↑ 2.1%"))
        metricsPanel.add(createMetricCard("Flaky Tests", "23", "↓ 8"))
        metricsPanel.add(createMetricCard("Avg Execution", "2m 34s", "↓ 15s"))
        metricsPanel.add(createMetricCard("Team Productivity", "85/100", "↑ 5"))
        metricsPanel.add(createMetricCard("Coverage", "78%", "↑ 3%"))
        
        panel.add(metricsPanel, BorderLayout.CENTER)
        
        val upgradePanel = JPanel(BorderLayout())
        upgradePanel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        
        val upgradeMessage = JLabel(
            "<html><p><b>💎 Upgrade to Team tier ($99/month) to unlock full analytics</b></p></html>"
        )
        val upgradeButton = JButton("Upgrade Now")
        upgradeButton.addActionListener {
            java.awt.Desktop.getDesktop().browse(
                java.net.URI("https://plugins.jetbrains.com/plugin/PSELENIUMIQAIPO/pricing")
            )
        }
        
        upgradePanel.add(upgradeMessage, BorderLayout.CENTER)
        upgradePanel.add(upgradeButton, BorderLayout.EAST)
        panel.add(upgradePanel, BorderLayout.SOUTH)
        
        return panel
    }

    private fun createMetricCard(title: String, value: String, change: String): JPanel {
        val card = JPanel(BorderLayout())
        card.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(java.awt.Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        )
        
        val titleLabel = JLabel(title)
        val valueLabel = JLabel("<html><h1>$value</h1></html>")
        val changeLabel = JLabel(change)
        changeLabel.foreground = if (change.startsWith("↑")) {
            java.awt.Color(0, 150, 0)
        } else {
            java.awt.Color(200, 0, 0)
        }
        
        card.add(titleLabel, BorderLayout.NORTH)
        card.add(valueLabel, BorderLayout.CENTER)
        card.add(changeLabel, BorderLayout.SOUTH)
        
        return card
    }
}
