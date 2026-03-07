package com.jakubjirak.seleniumiq.features

import com.intellij.openapi.project.Project
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.openapi.vfs.VfsUtilCore

data class APIEndpoint(
    val method: String,
    val url: String,
    val headers: Map<String, String>,
    val body: String?,
    val expectedStatus: Int
)

class APITestGenerator(private val project: Project) {
    
    fun generateAPITest(endpoint: APIEndpoint, testName: String, framework: String = "RestAssured"): String? {
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
    
    /**
     * Scans the project for API endpoint definitions by analyzing source files
     * for common framework annotations (Spring, JAX-RS) and OpenAPI/Swagger specs.
     */
    fun interceptNetworkCalls(): List<APIEndpoint> {
        val endpoints = mutableListOf<APIEndpoint>()
        endpoints.addAll(scanForSpringEndpoints())
        endpoints.addAll(scanForOpenAPIEndpoints())
        return endpoints
    }

    private fun scanForSpringEndpoints(): List<APIEndpoint> {
        val endpoints = mutableListOf<APIEndpoint>()
        val scope = GlobalSearchScope.projectScope(project)

        val annotationPatterns = mapOf(
            Regex("""@GetMapping\s*\(\s*(?:value\s*=\s*)?["']([^"']+)["']""") to "GET",
            Regex("""@PostMapping\s*\(\s*(?:value\s*=\s*)?["']([^"']+)["']""") to "POST",
            Regex("""@PutMapping\s*\(\s*(?:value\s*=\s*)?["']([^"']+)["']""") to "PUT",
            Regex("""@DeleteMapping\s*\(\s*(?:value\s*=\s*)?["']([^"']+)["']""") to "DELETE",
            Regex("""@PatchMapping\s*\(\s*(?:value\s*=\s*)?["']([^"']+)["']""") to "PATCH",
            Regex("""@RequestMapping\s*\([^)]*method\s*=\s*RequestMethod\.(\w+)[^)]*value\s*=\s*["']([^"']+)["']""") to "REQUEST_MAPPING",
        )

        val jaxRsPatterns = mapOf(
            Regex("""@GET\s+@Path\s*\(\s*["']([^"']+)["']""") to "GET",
            Regex("""@POST\s+@Path\s*\(\s*["']([^"']+)["']""") to "POST",
            Regex("""@PUT\s+@Path\s*\(\s*["']([^"']+)["']""") to "PUT",
            Regex("""@DELETE\s+@Path\s*\(\s*["']([^"']+)["']""") to "DELETE",
        )

        val extensions = listOf("java", "kt")
        for (ext in extensions) {
            val files = FilenameIndex.getAllFilesByExt(project, ext, scope)
            for (file in files) {
                try {
                    val content = VfsUtilCore.loadText(file)

                    val classPathMatch = Regex("""@RequestMapping\s*\(\s*(?:value\s*=\s*)?["']([^"']+)["']""").find(content)
                    val basePath = classPathMatch?.groupValues?.get(1) ?: ""

                    for ((pattern, method) in annotationPatterns) {
                        for (match in pattern.findAll(content)) {
                            val (path, resolvedMethod) = if (method == "REQUEST_MAPPING") {
                                Pair(match.groupValues[2], match.groupValues[1])
                            } else {
                                Pair(match.groupValues[1], method)
                            }
                            val fullPath = basePath + path
                            val needsBody = resolvedMethod in listOf("POST", "PUT", "PATCH")
                            endpoints.add(APIEndpoint(
                                method = resolvedMethod,
                                url = fullPath,
                                headers = mapOf("Content-Type" to "application/json", "Accept" to "application/json"),
                                body = if (needsBody) "{}" else null,
                                expectedStatus = if (resolvedMethod == "POST") 201 else 200
                            ))
                        }
                    }

                    for ((pattern, httpMethod) in jaxRsPatterns) {
                        for (match in pattern.findAll(content)) {
                            val path = basePath + match.groupValues[1]
                            val needsBody = httpMethod in listOf("POST", "PUT")
                            endpoints.add(APIEndpoint(
                                method = httpMethod,
                                url = path,
                                headers = mapOf("Content-Type" to "application/json", "Accept" to "application/json"),
                                body = if (needsBody) "{}" else null,
                                expectedStatus = if (httpMethod == "POST") 201 else 200
                            ))
                        }
                    }
                } catch (_: Exception) {
                    // Skip files that cannot be read
                }
            }
        }
        return endpoints
    }

    private fun scanForOpenAPIEndpoints(): List<APIEndpoint> {
        val endpoints = mutableListOf<APIEndpoint>()
        val scope = GlobalSearchScope.projectScope(project)

        val specFiles = listOf("openapi.yaml", "openapi.yml", "openapi.json", "swagger.yaml", "swagger.yml", "swagger.json")
        for (specName in specFiles) {
            val files = FilenameIndex.getVirtualFilesByName(specName, scope)
            for (file in files) {
                try {
                    val content = VfsUtilCore.loadText(file)
                    endpoints.addAll(parseOpenAPISpec(content))
                } catch (_: Exception) {
                    // Skip unreadable spec files
                }
            }
        }
        return endpoints
    }

    private fun parseOpenAPISpec(content: String): List<APIEndpoint> {
        val endpoints = mutableListOf<APIEndpoint>()
        val httpMethods = listOf("get", "post", "put", "delete", "patch")

        val pathPattern = Regex("""^\s{2}(/[^\s:]+)\s*:""", RegexOption.MULTILINE)
        val pathMatches = pathPattern.findAll(content).toList()

        for (pathMatch in pathMatches) {
            val path = pathMatch.groupValues[1]
            val pathStartIdx = pathMatch.range.last
            val nextPathIdx = pathMatches.firstOrNull { it.range.first > pathMatch.range.first }?.range?.first ?: content.length
            val pathBlock = content.substring(pathStartIdx, nextPathIdx)

            for (method in httpMethods) {
                val methodPattern = Regex("""^\s{4}$method\s*:""", RegexOption.MULTILINE)
                if (methodPattern.containsMatchIn(pathBlock)) {
                    val needsBody = method in listOf("post", "put", "patch")
                    endpoints.add(APIEndpoint(
                        method = method.uppercase(),
                        url = path,
                        headers = mapOf("Content-Type" to "application/json", "Accept" to "application/json"),
                        body = if (needsBody) "{}" else null,
                        expectedStatus = if (method == "post") 201 else 200
                    ))
                }
            }
        }
        return endpoints
    }
    
    companion object {
        private val instances = mutableMapOf<Project, APITestGenerator>()
        
        fun getInstance(project: Project): APITestGenerator {
            return instances.computeIfAbsent(project) { APITestGenerator(it) }
        }
    }
}
