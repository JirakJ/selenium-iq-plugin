package com.jakubjirak.seleniumiq.features


/** Generates starter code for reporting Selenium test results to test-management tools. */
class TestManagementService {
    
    fun generateJiraIntegrationCode(): String = JIRA_TEMPLATE.trimIndent()
    
    fun generateTestRailIntegrationCode(): String = TESTRAIL_TEMPLATE.trimIndent()

    private companion object {
        private const val JIRA_TEMPLATE = """
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
        """

        private const val TESTRAIL_TEMPLATE = """
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
        """
    }
}
