package com.jakubjirak.seleniumiq.features

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.io.File
import java.io.FileReader
import com.google.gson.Gson
import com.google.gson.JsonParser
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser

@Service(Service.Level.PROJECT)
class DataDrivenTestingService(private val project: Project) {
    
    private val gson = Gson()
    
    data class TestData(
        val name: String,
        val data: List<Map<String, Any>>
    )
    
    fun loadTestDataFromCsv(filePath: String): TestData {
        val file = File(filePath)
        if (!file.exists()) {
            throw IllegalArgumentException("CSV file not found: $filePath")
        }
        
        val records = mutableListOf<Map<String, Any>>()
        
        FileReader(file).use { reader ->
            val csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build()
            val csvParser = CSVParser(reader, csvFormat)
            
            csvParser.forEach { record ->
                val row = mutableMapOf<String, Any>()
                record.toMap().forEach { (key, value) ->
                    row[key] = parseValue(value)
                }
                records.add(row)
            }
        }
        
        return TestData(
            name = file.nameWithoutExtension,
            data = records
        )
    }
    
    fun loadTestDataFromJson(filePath: String): TestData {
        val file = File(filePath)
        if (!file.exists()) {
            throw IllegalArgumentException("JSON file not found: $filePath")
        }
        
        val jsonElement = JsonParser.parseReader(FileReader(file))
        val records = mutableListOf<Map<String, Any>>()
        
        if (jsonElement.isJsonArray) {
            jsonElement.asJsonArray.forEach { element ->
                if (element.isJsonObject) {
                    val row = mutableMapOf<String, Any>()
                    element.asJsonObject.entrySet().forEach { (key, value) ->
                        row[key] = when {
                            value.isJsonPrimitive -> {
                                val primitive = value.asJsonPrimitive
                                when {
                                    primitive.isNumber -> primitive.asDouble
                                    primitive.isBoolean -> primitive.asBoolean
                                    else -> primitive.asString
                                }
                            }
                            else -> value.toString()
                        }
                    }
                    records.add(row)
                }
            }
        }
        
        return TestData(
            name = file.nameWithoutExtension,
            data = records
        )
    }
    
    fun generateDataDrivenTest(
        testName: String,
        testData: TestData,
        testTemplate: String
    ): String {
        return buildString {
            appendLine("@Test")
            appendLine("@DisplayName(\"$testName\")")
            appendLine("@ParameterizedTest")
            appendLine("@MethodSource(\"${testData.name}DataProvider\")")
            appendLine("public void $testName(${generateParameterList(testData.data.first())}) {")
            appendLine("    // Generated from template")
            appendLine(testTemplate.prependIndent("    "))
            appendLine("}")
            appendLine()
            appendLine("private static Stream<Arguments> ${testData.name}DataProvider() {")
            appendLine("    return Stream.of(")
            testData.data.forEachIndexed { index, row ->
                val args = row.values.joinToString(", ") { value ->
                    when (value) {
                        is String -> "\"$value\""
                        is Number -> value.toString()
                        is Boolean -> value.toString()
                        else -> "\"$value\""
                    }
                }
                val comma = if (index < testData.data.size - 1) "," else ""
                appendLine("        Arguments.of($args)$comma")
            }
            appendLine("    );")
            appendLine("}")
        }
    }
    
    private fun generateParameterList(row: Map<String, Any>): String {
        return row.entries.joinToString(", ") { (key, value) ->
            val type = when (value) {
                is String -> "String"
                is Int -> "int"
                is Long -> "long"
                is Double -> "double"
                is Boolean -> "boolean"
                else -> "Object"
            }
            "$type $key"
        }
    }
    
    private fun parseValue(value: String): Any {
        return when {
            value.equals("true", ignoreCase = true) -> true
            value.equals("false", ignoreCase = true) -> false
            value.toIntOrNull() != null -> value.toInt()
            value.toDoubleOrNull() != null -> value.toDouble()
            else -> value
        }
    }
    
    fun validateTestData(testData: TestData): List<String> {
        val issues = mutableListOf<String>()
        
        if (testData.data.isEmpty()) {
            issues.add("Test data is empty")
            return issues
        }
        
        val expectedKeys = testData.data.first().keys
        testData.data.forEachIndexed { index, row ->
            val missingKeys = expectedKeys - row.keys
            val extraKeys = row.keys - expectedKeys
            
            if (missingKeys.isNotEmpty()) {
                issues.add("Row $index missing keys: ${missingKeys.joinToString()}")
            }
            if (extraKeys.isNotEmpty()) {
                issues.add("Row $index has extra keys: ${extraKeys.joinToString()}")
            }
            
            row.forEach { (key, value) ->
                if (value is String && value.isBlank()) {
                    issues.add("Row $index has blank value for key: $key")
                }
            }
        }
        
        return issues
    }
}
