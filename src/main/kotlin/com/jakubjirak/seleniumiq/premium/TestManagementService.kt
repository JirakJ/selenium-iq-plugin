package com.jakubjirak.seleniumiq.premium

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.time.LocalDateTime

@Service(Service.Level.PROJECT)
class TestManagementService(private val project: Project) {
    
    data class TestCase(
        val id: String,
        val title: String,
        val description: String,
        val priority: Priority,
        val status: TestStatus,
        val automationStatus: AutomationStatus,
        val tags: List<String> = emptyList(),
        val requirements: List<String> = emptyList()
    )
    
    data class TestRun(
        val id: String,
        val testCaseId: String,
        val status: TestStatus,
        val executedAt: LocalDateTime,
        val duration: Long,
        val error: String? = null,
        val screenshots: List<String> = emptyList()
    )
    
    enum class Priority { CRITICAL, HIGH, MEDIUM, LOW }
    enum class TestStatus { PASSED, FAILED, BLOCKED, SKIPPED, NOT_RUN }
    enum class AutomationStatus { AUTOMATED, MANUAL, IN_PROGRESS, NOT_AUTOMATED }
    
    enum class TestManagementPlatform {
        JIRA_ZEPHYR,
        TEST_RAIL,
        QTEST,
        X_RAY,
        ZEPHYR_SCALE
    }
    
    fun generateJiraIntegrationCode(): String {
        return """
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;

public class JiraIntegration {
    
    private static final String JIRA_URL = "https://your-domain.atlassian.net";
    private static final String API_TOKEN = System.getenv("JIRA_API_TOKEN");
    private static final String EMAIL = System.getenv("JIRA_EMAIL");
    
    @AfterMethod
    public void updateJiraTestCase(ITestResult result) {
        String testCaseId = getTestCaseId(result);
        String status = result.isSuccess() ? "PASS" : "FAIL";
        
        String payload = String.format(
            "{\"status\": \"%s\", \"comment\": \"%s\"}", 
            status, 
            result.getThrowable() != null ? result.getThrowable().getMessage() : "Test passed"
        );
        
        Response response = RestAssured
            .given()
                .auth().preemptive().basic(EMAIL, API_TOKEN)
                .header("Content-Type", "application/json")
                .body(payload)
            .when()
                .post(JIRA_URL + "/rest/atm/1.0/testrun/" + testCaseId + "/status")
            .then()
                .extract().response();
        
        System.out.println("Jira updated: " + response.getStatusCode());
    }
    
    private String getTestCaseId(ITestResult result) {
        // Extract test case ID from test method annotation or name
        return result.getMethod().getMethodName();
    }
}
        """.trimIndent()
    }
    
    fun generateTestRailIntegrationCode(): String {
        return """
import com.gurock.testrail.APIClient;
import com.gurock.testrail.APIException;
import org.json.simple.JSONObject;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;

public class TestRailIntegration {
    
    private static final String TESTRAIL_URL = "https://your-domain.testrail.io";
    private static final String USERNAME = System.getenv("TESTRAIL_USERNAME");
    private static final String PASSWORD = System.getenv("TESTRAIL_PASSWORD");
    
    private APIClient client;
    
    public TestRailIntegration() {
        client = new APIClient(TESTRAIL_URL);
        client.setUser(USERNAME);
        client.setPassword(PASSWORD);
    }
    
    @AfterMethod
    public void updateTestRail(ITestResult result) throws APIException {
        long testCaseId = getTestCaseId(result);
        long runId = getTestRunId();
        
        JSONObject data = new JSONObject();
        data.put("status_id", result.isSuccess() ? 1 : 5); // 1=Passed, 5=Failed
        data.put("comment", result.isSuccess() ? 
            "Test passed via SeleniumIQ" : 
            "Test failed: " + result.getThrowable().getMessage());
        
        if (!result.isSuccess() && hasScreenshot(result)) {
            data.put("defects", getScreenshotUrl(result));
        }
        
        client.sendPost("add_result_for_case/" + runId + "/" + testCaseId, data);
        System.out.println("TestRail updated for test case: " + testCaseId);
    }
    
    private long getTestCaseId(ITestResult result) {
        // Extract from annotation or test name
        return 123L;
    }
    
    private long getTestRunId() {
        return Long.parseLong(System.getProperty("testrail.run.id", "1"));
    }
    
    private boolean hasScreenshot(ITestResult result) {
        return false; // Implement screenshot detection
    }
    
    private String getScreenshotUrl(ITestResult result) {
        return ""; // Implement screenshot URL retrieval
    }
}
        """.trimIndent()
    }
    
    fun generateTraceabilityMatrix(testCases: List<TestCase>): String {
        val sb = StringBuilder()
        sb.appendLine("# Requirements Traceability Matrix")
        sb.appendLine()
        sb.appendLine("Generated: ${LocalDateTime.now()}")
        sb.appendLine()
        sb.appendLine("| Requirement | Test Cases | Status | Automation | Priority |")
        sb.appendLine("|-------------|------------|--------|------------|----------|")
        
        val requirementMap = mutableMapOf<String, MutableList<TestCase>>()
        testCases.forEach { tc ->
            tc.requirements.forEach { req ->
                requirementMap.getOrPut(req) { mutableListOf() }.add(tc)
            }
        }
        
        requirementMap.forEach { (req, cases) ->
            val caseIds = cases.joinToString(", ") { it.id }
            val status = if (cases.all { it.status == TestStatus.PASSED }) "✅ Pass" else "❌ Fail"
            val automation = "${cases.count { it.automationStatus == AutomationStatus.AUTOMATED }}/${cases.size}"
            val priority = cases.maxByOrNull { it.priority }?.priority ?: Priority.LOW
            sb.appendLine("| $req | $caseIds | $status | $automation | $priority |")
        }
        
        sb.appendLine()
        sb.appendLine("## Summary")
        sb.appendLine("- Total Requirements: ${requirementMap.size}")
        sb.appendLine("- Total Test Cases: ${testCases.size}")
        sb.appendLine("- Automated: ${testCases.count { it.automationStatus == AutomationStatus.AUTOMATED}}")
        sb.appendLine("- Manual: ${testCases.count { it.automationStatus == AutomationStatus.MANUAL }}")
        sb.appendLine("- Coverage: ${String.format("%.1f", testCases.size.toDouble() / requirementMap.size * 100)}%")
        
        return sb.toString()
    }
    
    companion object {
        fun getInstance(project: Project): TestManagementService {
            return project.getService(TestManagementService::class.java)
        }
    }
}
