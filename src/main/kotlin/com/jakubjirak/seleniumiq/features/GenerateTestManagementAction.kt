/*
 * Copyright 2026 Jakub Jirák
 * SPDX-License-Identifier: Apache-2.0
 */
package com.jakubjirak.seleniumiq.features

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.ui.Messages
import java.awt.datatransfer.StringSelection

/** Copies a TestNG listener skeleton that reports results to Jira or TestRail to the clipboard. */
class GenerateTestManagementAction : AnAction("Generate Test Management Integration") {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val choice = Messages.showEditableChooseDialog(
            "Select test management platform:", "Generate Test Management Integration",
            Messages.getQuestionIcon(), PLATFORMS, PLATFORMS[0], null
        ) ?: return
        val code = codeFor(choice) ?: return
        CopyPasteManager.getInstance().setContents(StringSelection(code))
        Messages.showInfoMessage(
            project,
            "A $choice TestNG integration skeleton was copied to the clipboard.\n" +
                "It reads credentials from environment variables; complete the placeholder methods for your instance.",
            "Test Management Integration"
        )
    }

    companion object {
        val PLATFORMS = arrayOf("Jira", "TestRail")

        /** Generated skeleton for [platform], or null for an unknown platform. */
        fun codeFor(platform: String): String? {
            val generator = TestManagementService()
            return when (platform) {
                "Jira" -> generator.generateJiraIntegrationCode()
                "TestRail" -> generator.generateTestRailIntegrationCode()
                else -> null
            }
        }
    }
}
