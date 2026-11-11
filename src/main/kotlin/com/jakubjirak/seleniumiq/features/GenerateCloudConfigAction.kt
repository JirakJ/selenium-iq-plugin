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

/** Copies a RemoteWebDriver starter class for the chosen Selenium cloud grid to the clipboard. */
class GenerateCloudConfigAction : AnAction("Generate Cloud Platform Config") {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val choice = Messages.showEditableChooseDialog(
            "Select cloud platform:", "Generate Cloud Configuration", Messages.getQuestionIcon(),
            PLATFORMS, PLATFORMS[0], null
        ) ?: return
        val code = codeFor(choice) ?: return
        CopyPasteManager.getInstance().setContents(StringSelection(code))
        Messages.showInfoMessage(
            project,
            "A $choice RemoteWebDriver test class was copied to the clipboard.\n" +
                "Paste it into your test sources and replace YOUR_USERNAME / YOUR_KEY " +
                "(better: read them from environment variables).",
            "Cloud Configuration Generated"
        )
    }

    companion object {
        val PLATFORMS = arrayOf("BrowserStack", "Sauce Labs", "LambdaTest")

        /** Generated class for [platform], or null for an unknown platform. */
        fun codeFor(platform: String): String? {
            val generator = CloudPlatformService()
            return when (platform) {
                "BrowserStack" -> generator.generateBrowserStackCode("YOUR_USERNAME", "YOUR_KEY")
                "Sauce Labs" -> generator.generateSauceLabsCode("YOUR_USERNAME", "YOUR_KEY")
                "LambdaTest" -> generator.generateLambdaTestCode("YOUR_USERNAME", "YOUR_KEY")
                else -> null
            }
        }
    }
}
