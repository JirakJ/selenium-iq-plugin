package com.jakubjirak.seleniumiq.premium

import com.intellij.openapi.project.Project
import com.jakubjirak.seleniumiq.licensing.LicenseManager

data class APIEndpoint(
    val method: String,
    val url: String,
    val headers: Map<String, String>,
    val body: String?,
    val expectedStatus: Int
)

class APITestGenerator(private val project: Project) {
    private val licenseManager = LicenseManager.getInstance()
    
    fun generateAPITest(endpoint: APIEndpoint, testName: String, framework: String = "RestAssured"): String? {
        if (!licenseManager.hasFeature("api_test_generation")) {
            return null
        }
        
        return when (framework) {
            "RestAssured" -> generateRestAssuredTest(endpoint, testName)
            "OkHttp" -> generateOkHttpTest(endpoint, testName)
            "Apache" -> generateApacheHttpTest(endpoint, testName)
            else -> null
        }
    }
    
    private fun generateRestAssuredTest(endpoint: APIEndpoint, testName: String): String {
        val sb = StringBuilder()
        
        sb.appendLine("@Test")
        sb.appendLine("public void $testName() {")
        sb.append("    given()")
        
        if (endpoint.headers.isNotEmpty()) {
            sb.appendLine()
            endpoint.headers.forEach { (key, value) ->
                sb.appendLine("        .header(\"$key\", \"$value\")")
            }
        }
        
        if (endpoint.body != null) {
            sb.appendLine("        .body(\"${endpoint.body.replace("\"", "\\\"")}\")")
        }
        
        sb.appendLine("    .when()")
        sb.appendLine("        .${endpoint.method.lowercase()}(\"${endpoint.url}\")")
        sb.appendLine("    .then()")
        sb.appendLine("        .statusCode(${endpoint.expectedStatus});")
        sb.appendLine("}")
        
        return sb.toString()
    }
    
    private fun generateOkHttpTest(endpoint: APIEndpoint, testName: String): String {
        val sb = StringBuilder()
        
        sb.appendLine("@Test")
        sb.appendLine("public void $testName() throws IOException {")
        sb.appendLine("    OkHttpClient client = new OkHttpClient();")
        
        if (endpoint.body != null) {
            sb.appendLine("    RequestBody body = RequestBody.create(")
            sb.appendLine("        \"${endpoint.body.replace("\"", "\\\"")}\",")
            sb.appendLine("        MediaType.parse(\"application/json\")")
            sb.appendLine("    );")
        }
        
        sb.appendLine("    Request.Builder builder = new Request.Builder()")
        sb.appendLine("        .url(\"${endpoint.url}\")")
        
        endpoint.headers.forEach { (key, value) ->
            sb.appendLine("        .addHeader(\"$key\", \"$value\")")
        }
        
        if (endpoint.body != null) {
            sb.appendLine("        .${endpoint.method.lowercase()}(body);")
        } else {
            sb.appendLine("        .${endpoint.method.lowercase()}();")
        }
        
        sb.appendLine("    ")
        sb.appendLine("    Response response = client.newCall(builder.build()).execute();")
        sb.appendLine("    Assert.assertEquals(${endpoint.expectedStatus}, response.code());")
        sb.appendLine("}")
        
        return sb.toString()
    }
    
    private fun generateApacheHttpTest(endpoint: APIEndpoint, testName: String): String {
        val sb = StringBuilder()
        
        sb.appendLine("@Test")
        sb.appendLine("public void $testName() throws Exception {")
        sb.appendLine("    CloseableHttpClient client = HttpClients.createDefault();")
        
        when (endpoint.method.uppercase()) {
            "GET" -> {
                sb.appendLine("    HttpGet request = new HttpGet(\"${endpoint.url}\");")
            }
            "POST" -> {
                sb.appendLine("    HttpPost request = new HttpPost(\"${endpoint.url}\");")
                if (endpoint.body != null) {
                    sb.appendLine("    request.setEntity(new StringEntity(\"${endpoint.body.replace("\"", "\\\"")}\"));")
                }
            }
            "PUT" -> {
                sb.appendLine("    HttpPut request = new HttpPut(\"${endpoint.url}\");")
                if (endpoint.body != null) {
                    sb.appendLine("    request.setEntity(new StringEntity(\"${endpoint.body.replace("\"", "\\\"")}\"));")
                }
            }
            "DELETE" -> {
                sb.appendLine("    HttpDelete request = new HttpDelete(\"${endpoint.url}\");")
            }
        }
        
        endpoint.headers.forEach { (key, value) ->
            sb.appendLine("    request.addHeader(\"$key\", \"$value\");")
        }
        
        sb.appendLine("    ")
        sb.appendLine("    CloseableHttpResponse response = client.execute(request);")
        sb.appendLine("    Assert.assertEquals(${endpoint.expectedStatus}, response.getStatusLine().getStatusCode());")
        sb.appendLine("    client.close();")
        sb.appendLine("}")
        
        return sb.toString()
    }
    
    fun interceptNetworkCalls(): List<APIEndpoint> {
        if (!licenseManager.hasFeature("api_test_generation")) {
            return emptyList()
        }
        
        // This would integrate with browser DevTools protocol to capture network traffic
        // For now, return empty list as placeholder
        return emptyList()
    }
    
    companion object {
        private val instances = mutableMapOf<Project, APITestGenerator>()
        
        fun getInstance(project: Project): APITestGenerator {
            return instances.computeIfAbsent(project) { APITestGenerator(it) }
        }
    }
}
