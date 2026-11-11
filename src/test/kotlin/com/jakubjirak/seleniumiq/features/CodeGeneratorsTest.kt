package com.jakubjirak.seleniumiq.features

import com.jakubjirak.seleniumiq.feedback.FeedbackNotifier
import com.jakubjirak.seleniumiq.feedback.FeedbackState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class CodeGeneratorsTest {

    @Test
    fun `every offered cloud platform produces a RemoteWebDriver class`() {
        val expected = mapOf(
            "BrowserStack" to "hub-cloud.browserstack.com",
            "Sauce Labs" to "saucelabs.com",
            "LambdaTest" to "lambdatest.com"
        )
        assertEquals(expected.keys.toList(), GenerateCloudConfigAction.PLATFORMS.toList())
        expected.forEach { (platform, host) ->
            val code = GenerateCloudConfigAction.codeFor(platform)!!
            assertTrue(code.contains("RemoteWebDriver"), platform)
            assertTrue(code.contains(host), platform)
            assertTrue(code.contains("YOUR_USERNAME") && code.contains("YOUR_KEY"), platform)
            assertFalse(code.startsWith(" "), "generated code is trimmed")
        }
        assertNull(GenerateCloudConfigAction.codeFor("Perfecto"))
    }

    @Test
    fun `cloud code embeds the given credentials`() {
        val code = CloudPlatformService().generateSauceLabsCode("alice", "k3y")
        assertTrue(code.contains("alice") && code.contains("k3y"))
    }

    @Test
    fun `every offered test management platform produces a listener reading env credentials`() {
        assertEquals(listOf("Jira", "TestRail"), GenerateTestManagementAction.PLATFORMS.toList())
        val jira = GenerateTestManagementAction.codeFor("Jira")!!
        assertTrue(jira.contains("class JiraIntegration"))
        assertTrue(jira.contains("System.getenv(\"JIRA_API_TOKEN\")"))
        val testRail = GenerateTestManagementAction.codeFor("TestRail")!!
        assertTrue(testRail.contains("class TestRailIntegration"))
        assertTrue(testRail.contains("System.getenv(\"TESTRAIL_PASSWORD\")"))
        assertNull(GenerateTestManagementAction.codeFor("qTest"))
    }

    @Test
    fun `feedback prompt waits a week and is shown once`() {
        val day = TimeUnit.DAYS.toMillis(1)
        val now = 1_700_000_000_000L
        val fresh = FeedbackState.State()
        assertFalse(FeedbackNotifier.shouldShowFeedback(fresh, now))
        assertEquals(now, fresh.firstActivation)
        assertFalse(FeedbackNotifier.shouldShowFeedback(FeedbackState.State(now), now + 7 * day - 1))
        assertTrue(FeedbackNotifier.shouldShowFeedback(FeedbackState.State(now), now + 7 * day))
        assertFalse(FeedbackNotifier.shouldShowFeedback(FeedbackState.State(now, feedbackShown = true), now + 30 * day))
        assertFalse(FeedbackNotifier.shouldShowFeedback(FeedbackState.State(now, dismissed = true), now + 30 * day))
    }

    @Test
    fun `plugin xml registers what the code uses and nothing that was removed`() {
        val xml = javaClass.getResource("/META-INF/plugin.xml")!!.readText()
        assertTrue(xml.contains("<notificationGroup id=\"${FeedbackNotifier.NOTIFICATION_GROUP_ID}\""))
        listOf("TeamAnalyticsDashboard", "VisualRegressionAction", "RequirementsTraceabilityAction", "86% accuracy")
            .forEach { assertFalse(xml.contains(it), it) }
    }
}
