package com.jakubjirak.seleniumiq.features

import com.intellij.openapi.project.Project
import java.time.Instant

data class RecordedAction(
    val type: ActionType,
    val locator: String,
    val value: String?,
    val timestamp: Instant,
    val screenshot: String?
)

enum class ActionType {
    CLICK,
    TYPE,
    SELECT,
    NAVIGATE,
    WAIT,
    ASSERT,
    SCROLL
}

data class RecordedTest(
    val name: String,
    val actions: List<RecordedAction>,
    val startTime: Instant,
    val endTime: Instant?,
    val url: String
)

class TestRecorder(private val project: Project) {
    private var currentRecording: MutableList<RecordedAction>? = null
    private var recordingStartTime: Instant? = null
    private var recordingUrl: String? = null
    
    fun startRecording(url: String): Boolean {
        if (currentRecording != null) {
            return false // Already recording
        }
        
        currentRecording = mutableListOf()
        recordingStartTime = Instant.now()
        recordingUrl = url
        return true
    }
    
    fun stopRecording(testName: String): RecordedTest? {
        val actions = currentRecording ?: return null
        val startTime = recordingStartTime ?: return null
        val url = recordingUrl ?: return null
        
        val test = RecordedTest(
            name = testName,
            actions = actions.toList(),
            startTime = startTime,
            endTime = Instant.now(),
            url = url
        )
        
        currentRecording = null
        recordingStartTime = null
        recordingUrl = null
        
        return test
    }
    
    fun recordAction(action: RecordedAction): Boolean {
        currentRecording?.add(action) ?: return false
        return true
    }
    
    fun isRecording(): Boolean {
        return currentRecording != null
    }
    
    fun generateTestCode(test: RecordedTest, framework: String = "JUnit"): String {
        val sb = StringBuilder()
        
        when (framework) {
            "JUnit" -> {
                sb.appendLine("@Test")
                sb.appendLine("public void ${test.name}() {")
                sb.appendLine("    driver.get(\"${test.url}\");")
                
                for (action in test.actions) {
                    when (action.type) {
                        ActionType.CLICK -> {
                            sb.appendLine("    driver.findElement(By.${action.locator}).click();")
                        }
                        ActionType.TYPE -> {
                            sb.appendLine("    driver.findElement(By.${action.locator}).sendKeys(\"${action.value}\");")
                        }
                        ActionType.SELECT -> {
                            sb.appendLine("    new Select(driver.findElement(By.${action.locator})).selectByVisibleText(\"${action.value}\");")
                        }
                        ActionType.NAVIGATE -> {
                            sb.appendLine("    driver.get(\"${action.value}\");")
                        }
                        ActionType.WAIT -> {
                            sb.appendLine("    new WebDriverWait(driver, Duration.ofSeconds(10))")
                            sb.appendLine("        .until(ExpectedConditions.visibilityOfElementLocated(By.${action.locator}));")
                        }
                        ActionType.ASSERT -> {
                            sb.appendLine("    Assert.assertEquals(\"${action.value}\", driver.findElement(By.${action.locator}).getText());")
                        }
                        ActionType.SCROLL -> {
                            sb.appendLine("    ((JavascriptExecutor) driver).executeScript(\"arguments[0].scrollIntoView(true);\", driver.findElement(By.${action.locator}));")
                        }
                    }
                }
                
                sb.appendLine("}")
            }
            "TestNG" -> {
                sb.appendLine("@Test")
                sb.appendLine("public void ${test.name}() {")
                sb.appendLine("    driver.get(\"${test.url}\");")
                
                for (action in test.actions) {
                    when (action.type) {
                        ActionType.CLICK -> {
                            sb.appendLine("    driver.findElement(By.${action.locator}).click();")
                        }
                        ActionType.TYPE -> {
                            sb.appendLine("    driver.findElement(By.${action.locator}).sendKeys(\"${action.value}\");")
                        }
                        ActionType.ASSERT -> {
                            sb.appendLine("    Assert.assertEquals(driver.findElement(By.${action.locator}).getText(), \"${action.value}\");")
                        }
                        else -> {} // Similar to JUnit
                    }
                }
                
                sb.appendLine("}")
            }
        }
        
        return sb.toString()
    }
    
    companion object {
        private val instances = mutableMapOf<Project, TestRecorder>()
        
        fun getInstance(project: Project): TestRecorder {
            return instances.computeIfAbsent(project) { TestRecorder(it) }
        }
    }
}
