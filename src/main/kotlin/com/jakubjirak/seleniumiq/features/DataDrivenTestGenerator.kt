package com.jakubjirak.seleniumiq.features

import com.intellij.openapi.project.Project

data class TestDataRow(
    val values: Map<String, String>
)

data class DataDrivenTest(
    val testName: String,
    val testCode: String,
    val dataSource: String,
    val parameters: List<String>
)

class DataDrivenTestGenerator(private val project: Project) {
    
    fun generateDataDrivenTest(
        testName: String,
        testCode: String,
        parameters: List<String>,
        dataSource: DataSource,
        framework: String = "JUnit"
    ): String? {
        return when (framework) {
            "JUnit" -> generateJUnitParameterizedTest(testName, testCode, parameters, dataSource)
            "TestNG" -> generateTestNGDataProviderTest(testName, testCode, parameters, dataSource)
            else -> null
        }
    }
    
    private fun generateJUnitParameterizedTest(
        testName: String,
        testCode: String,
        parameters: List<String>,
        dataSource: DataSource
    ): String {
        val sb = StringBuilder()
        
        sb.appendLine("@ParameterizedTest")
        when (dataSource) {
            is DataSource.CSV -> {
                sb.appendLine("@CsvFileSource(resources = \"${dataSource.filePath}\", numLinesToSkip = 1)")
            }
            is DataSource.Excel -> {
                sb.appendLine("@MethodSource(\"${testName}DataProvider\")")
            }
            is DataSource.Database -> {
                sb.appendLine("@MethodSource(\"${testName}DataProvider\")")
            }
        }
        
        val paramString = parameters.joinToString(", ") { "String $it" }
        sb.appendLine("public void $testName($paramString) {")
        
        // Replace parameter placeholders in test code
        var modifiedCode = testCode
        parameters.forEach { param ->
            modifiedCode = modifiedCode.replace("\${$param}", "\" + $param + \"")
        }
        
        sb.appendLine(modifiedCode.prependIndent("    "))
        sb.appendLine("}")
        
        // Generate data provider method if needed
        when (dataSource) {
            is DataSource.Excel -> {
                sb.appendLine()
                sb.appendLine("private static Stream<Arguments> ${testName}DataProvider() throws Exception {")
                sb.appendLine("    Workbook workbook = WorkbookFactory.create(new File(\"${dataSource.filePath}\"));")
                sb.appendLine("    Sheet sheet = workbook.getSheetAt(0);")
                sb.appendLine("    List<Arguments> data = new ArrayList<>();")
                sb.appendLine("    ")
                sb.appendLine("    for (int i = 1; i <= sheet.getLastRowNum(); i++) {")
                sb.appendLine("        Row row = sheet.getRow(i);")
                val argsList = parameters.indices.joinToString(", ") { "row.getCell($it).getStringCellValue()" }
                sb.appendLine("        data.add(Arguments.of($argsList));")
                sb.appendLine("    }")
                sb.appendLine("    return data.stream();")
                sb.appendLine("}")
            }
            is DataSource.Database -> {
                sb.appendLine()
                sb.appendLine("private static Stream<Arguments> ${testName}DataProvider() throws Exception {")
                sb.appendLine("    Connection conn = DriverManager.getConnection(")
                sb.appendLine("        \"${dataSource.connectionString}\",")
                sb.appendLine("        \"${dataSource.username}\",")
                sb.appendLine("        \"${dataSource.password}\"")
                sb.appendLine("    );")
                sb.appendLine("    Statement stmt = conn.createStatement();")
                sb.appendLine("    ResultSet rs = stmt.executeQuery(\"${dataSource.query}\");")
                sb.appendLine("    List<Arguments> data = new ArrayList<>();")
                sb.appendLine("    ")
                sb.appendLine("    while (rs.next()) {")
                val argsList = parameters.mapIndexed { index, _ -> "rs.getString(${index + 1})" }.joinToString(", ")
                sb.appendLine("        data.add(Arguments.of($argsList));")
                sb.appendLine("    }")
                sb.appendLine("    conn.close();")
                sb.appendLine("    return data.stream();")
                sb.appendLine("}")
            }
            else -> {}
        }
        
        return sb.toString()
    }
    
    private fun generateTestNGDataProviderTest(
        testName: String,
        testCode: String,
        parameters: List<String>,
        dataSource: DataSource
    ): String {
        val sb = StringBuilder()
        
        sb.appendLine("@Test(dataProvider = \"${testName}Data\")")
        val paramString = parameters.joinToString(", ") { "String $it" }
        sb.appendLine("public void $testName($paramString) {")
        
        var modifiedCode = testCode
        parameters.forEach { param ->
            modifiedCode = modifiedCode.replace("\${$param}", "\" + $param + \"")
        }
        
        sb.appendLine(modifiedCode.prependIndent("    "))
        sb.appendLine("}")
        sb.appendLine()
        sb.appendLine("@DataProvider(name = \"${testName}Data\")")
        sb.appendLine("public Object[][] ${testName}DataProvider() throws Exception {")
        
        when (dataSource) {
            is DataSource.CSV -> {
                sb.appendLine("    CSVReader reader = new CSVReader(new FileReader(\"${dataSource.filePath}\"));")
                sb.appendLine("    List<String[]> allRows = reader.readAll();")
                sb.appendLine("    allRows.remove(0); // Remove header")
                sb.appendLine("    return allRows.toArray(new Object[0][]);")
            }
            is DataSource.Excel -> {
                sb.appendLine("    Workbook workbook = WorkbookFactory.create(new File(\"${dataSource.filePath}\"));")
                sb.appendLine("    Sheet sheet = workbook.getSheetAt(0);")
                sb.appendLine("    int rowCount = sheet.getLastRowNum();")
                sb.appendLine("    int colCount = ${parameters.size};")
                sb.appendLine("    Object[][] data = new Object[rowCount][colCount];")
                sb.appendLine("    ")
                sb.appendLine("    for (int i = 0; i < rowCount; i++) {")
                sb.appendLine("        Row row = sheet.getRow(i + 1);")
                sb.appendLine("        for (int j = 0; j < colCount; j++) {")
                sb.appendLine("            data[i][j] = row.getCell(j).getStringCellValue();")
                sb.appendLine("        }")
                sb.appendLine("    }")
                sb.appendLine("    return data;")
            }
            is DataSource.Database -> {
                sb.appendLine("    Connection conn = DriverManager.getConnection(")
                sb.appendLine("        \"${dataSource.connectionString}\",")
                sb.appendLine("        \"${dataSource.username}\",")
                sb.appendLine("        \"${dataSource.password}\"")
                sb.appendLine("    );")
                sb.appendLine("    Statement stmt = conn.createStatement();")
                sb.appendLine("    ResultSet rs = stmt.executeQuery(\"${dataSource.query}\");")
                sb.appendLine("    List<Object[]> data = new ArrayList<>();")
                sb.appendLine("    ")
                sb.appendLine("    while (rs.next()) {")
                val cols = parameters.mapIndexed { index, _ -> "rs.getString(${index + 1})" }.joinToString(", ")
                sb.appendLine("        data.add(new Object[]{$cols});")
                sb.appendLine("    }")
                sb.appendLine("    conn.close();")
                sb.appendLine("    return data.toArray(new Object[0][]);")
            }
        }
        
        sb.appendLine("}")
        
        return sb.toString()
    }
    
    companion object {
        private val instances = mutableMapOf<Project, DataDrivenTestGenerator>()
        
        fun getInstance(project: Project): DataDrivenTestGenerator {
            return instances.computeIfAbsent(project) { DataDrivenTestGenerator(it) }
        }
    }
}

sealed class DataSource {
    data class CSV(val filePath: String) : DataSource()
    data class Excel(val filePath: String) : DataSource()
    data class Database(
        val connectionString: String,
        val username: String,
        val password: String,
        val query: String
    ) : DataSource()
}
