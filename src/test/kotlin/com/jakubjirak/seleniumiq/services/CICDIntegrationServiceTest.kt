/*
 * Copyright (c) 2025 Jakub Jirák. All rights reserved.
 * Licensed under the proprietary license. See LICENSE for details.
 */
package com.jakubjirak.seleniumiq.services

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.lang.reflect.Constructor

class CICDIntegrationServiceTest {

    private fun createService(): CICDIntegrationService {
        val constructor: Constructor<CICDIntegrationService> =
            CICDIntegrationService::class.java.getDeclaredConstructor()
        constructor.isAccessible = true
        return constructor.newInstance()
    }

    @Test
    fun `generateJenkinsPipeline contains pipeline structure`() {
        val service = createService()
        val config = mapOf<String, Any>(
            "repo_url" to "https://github.com/example/repo.git",
            "notification_email" to "dev@example.com"
        )

        val pipeline = service.generateJenkinsPipeline("MyProject", config)

        assertTrue(pipeline.contains("pipeline {"))
        assertTrue(pipeline.contains("agent any"))
        assertTrue(pipeline.contains("stage('Checkout')"))
        assertTrue(pipeline.contains("stage('Build')"))
        assertTrue(pipeline.contains("stage('Run Selenium Tests')"))
        assertTrue(pipeline.contains("stage('Generate Report')"))
        assertTrue(pipeline.contains("https://github.com/example/repo.git"))
        assertTrue(pipeline.contains("dev@example.com"))
    }

    @Test
    fun `generateGitHubActions contains workflow structure`() {
        val service = createService()
        val config = mapOf<String, Any>()

        val workflow = service.generateGitHubActions("MyProject", config)

        assertTrue(workflow.contains("name: Selenium Tests"))
        assertTrue(workflow.contains("push:"))
        assertTrue(workflow.contains("pull_request:"))
        assertTrue(workflow.contains("runs-on: ubuntu-latest"))
        assertTrue(workflow.contains("browser: [chrome, firefox]"))
        assertTrue(workflow.contains("actions/checkout@v3"))
        assertTrue(workflow.contains("setup-java@v3"))
    }

    @Test
    fun `generateGitLabCI contains stages`() {
        val service = createService()
        val config = mapOf<String, Any>()

        val ci = service.generateGitLabCI("MyProject", config)

        assertTrue(ci.contains("image: maven:3.8-openjdk-17"))
        assertTrue(ci.contains("stages:"))
        assertTrue(ci.contains("- build"))
        assertTrue(ci.contains("- test"))
        assertTrue(ci.contains("- report"))
        assertTrue(ci.contains("test:chrome:"))
        assertTrue(ci.contains("test:firefox:"))
    }

    @Test
    fun `generateCircleCI contains jobs and workflows`() {
        val service = createService()
        val config = service.generateCircleCI("MyProject")

        assertTrue(config.contains("version: 2.1"))
        assertTrue(config.contains("orbs:"))
        assertTrue(config.contains("test:"))
        assertTrue(config.contains("workflows:"))
        assertTrue(config.contains("store_test_results:"))
    }

    @Test
    fun `generateCITemplates returns correct filename for Jenkins`() {
        val service = createService()
        val templates = service.generateCITemplates(
            CICDIntegrationService.CIProvider.JENKINS, "TestProject"
        )

        assertTrue(templates.containsKey("Jenkinsfile"))
        assertTrue(templates["Jenkinsfile"]!!.contains("pipeline"))
    }

    @Test
    fun `generateCITemplates returns correct path for GitHub Actions`() {
        val service = createService()
        val templates = service.generateCITemplates(
            CICDIntegrationService.CIProvider.GITHUB_ACTIONS, "TestProject"
        )

        assertTrue(templates.containsKey(".github/workflows/tests.yml"))
    }

    @Test
    fun `generateCITemplates returns correct path for GitLab CI`() {
        val service = createService()
        val templates = service.generateCITemplates(
            CICDIntegrationService.CIProvider.GITLAB_CI, "TestProject"
        )

        assertTrue(templates.containsKey(".gitlab-ci.yml"))
    }

    @Test
    fun `generateCITemplates returns correct path for CircleCI`() {
        val service = createService()
        val templates = service.generateCITemplates(
            CICDIntegrationService.CIProvider.CIRCLE_CI, "TestProject"
        )

        assertTrue(templates.containsKey(".circleci/config.yml"))
    }

    @Test
    fun `CIProvider enum has all expected values`() {
        val providers = CICDIntegrationService.CIProvider.values()
        assertEquals(7, providers.size)
        assertTrue(providers.any { it.name == "JENKINS" })
        assertTrue(providers.any { it.name == "GITHUB_ACTIONS" })
        assertTrue(providers.any { it.name == "AZURE_DEVOPS" })
        assertTrue(providers.any { it.name == "TEAMCITY" })
    }
}
