package com.jakubjirak.seleniumiq.services;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.psi.*;
import com.jakubjirak.seleniumiq.models.FlakinessScore;

import java.util.regex.Pattern;

@Service(Service.Level.APP)
public final class FlakinessAnalyzer {

    private static final Pattern THREAD_SLEEP_PATTERN = Pattern.compile("Thread\\.sleep\\s*\\(");
    private static final Pattern ABSOLUTE_XPATH_PATTERN = Pattern.compile("/html/body|/html\\[");
    private static final Pattern DYNAMIC_ID_PATTERN = Pattern.compile("\\d{5,}|[0-9a-f]{8}-[0-9a-f]{4}");

    public static FlakinessAnalyzer getInstance() {
        return ApplicationManager.getApplication().getService(FlakinessAnalyzer.class);
    }

    public FlakinessScore analyzeTest(PsiMethod testMethod) {
        FlakinessScore.TestMetrics metrics = new FlakinessScore.TestMetrics();
        
        if (testMethod.getBody() == null) {
            return FlakinessScore.calculateScore(metrics);
        }

        String methodText = testMethod.getBody().getText();
        
        // Detect Thread.sleep
        metrics.setHasThreadSleep(THREAD_SLEEP_PATTERN.matcher(methodText).find());
        
        // Detect absolute XPath
        metrics.setHasAbsoluteXPath(ABSOLUTE_XPATH_PATTERN.matcher(methodText).find());
        
        // Detect dynamic IDs
        metrics.setHasDynamicIds(DYNAMIC_ID_PATTERN.matcher(methodText).find());
        
        // Count assertions
        metrics.setAssertionCount(countAssertions(methodText));
        
        return FlakinessScore.calculateScore(metrics);
    }

    public FlakinessScore analyzeTestFile(PsiFile file) {
        FlakinessScore.TestMetrics aggregateMetrics = new FlakinessScore.TestMetrics();
        int testMethodCount = 0;
        
        file.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitMethod(PsiMethod method) {
                super.visitMethod(method);
                
                if (isTestMethod(method)) {
                    FlakinessScore score = analyzeTest(method);
                    // Aggregate metrics
                    if (score.getIndicators().stream().anyMatch(i -> i.getName().contains("Thread.sleep"))) {
                        aggregateMetrics.setHasThreadSleep(true);
                    }
                    if (score.getIndicators().stream().anyMatch(i -> i.getName().contains("XPath"))) {
                        aggregateMetrics.setHasAbsoluteXPath(true);
                    }
                    if (score.getIndicators().stream().anyMatch(i -> i.getName().contains("Dynamic"))) {
                        aggregateMetrics.setHasDynamicIds(true);
                    }
                }
            }
        });
        
        return FlakinessScore.calculateScore(aggregateMetrics);
    }

    private boolean isTestMethod(PsiMethod method) {
        PsiAnnotation[] annotations = method.getAnnotations();
        for (PsiAnnotation annotation : annotations) {
            String qualifiedName = annotation.getQualifiedName();
            if (qualifiedName != null && 
                (qualifiedName.contains("Test") || qualifiedName.contains("org.junit"))) {
                return true;
            }
        }
        return false;
    }

    private int countAssertions(String methodText) {
        int count = 0;
        String[] assertKeywords = {"assert", "Assert", "verify", "Verify", "assertEquals", "assertTrue"};
        
        for (String keyword : assertKeywords) {
            int index = 0;
            while ((index = methodText.indexOf(keyword, index)) != -1) {
                count++;
                index += keyword.length();
            }
        }
        
        return count;
    }

    public String generateFlakinessReport(FlakinessScore score) {
        StringBuilder report = new StringBuilder();
        report.append("Flakiness Analysis Report\n");
        report.append("========================\n\n");
        report.append("Overall Risk: ").append(score.getRiskLevel()).append("\n");
        report.append("Flakiness Score: ").append(String.format("%.1f%%", score.getOverallScore() * 100)).append("\n\n");
        
        report.append("Issues Detected:\n");
        for (FlakinessScore.FlakinessIndicator indicator : score.getIndicators()) {
            report.append("  • ").append(indicator.getName()).append(" [").append(indicator.getSeverity()).append("]\n");
            report.append("    ").append(indicator.getDescription()).append("\n");
        }
        
        report.append("\nRecommendations:\n");
        int recNum = 1;
        for (String recommendation : score.getRecommendations()) {
            report.append("  ").append(recNum++).append(". ").append(recommendation).append("\n");
        }
        
        return report.toString();
    }
}
