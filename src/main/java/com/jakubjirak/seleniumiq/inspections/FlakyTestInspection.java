package com.jakubjirak.seleniumiq.inspections;

import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import com.jakubjirak.seleniumiq.models.FlakinessScore;
import com.jakubjirak.seleniumiq.services.FlakinessAnalyzer;
import org.jetbrains.annotations.NotNull;

public class FlakyTestInspection extends AbstractBaseJavaLocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethod(PsiMethod method) {
                super.visitMethod(method);

                if (!isTestMethod(method)) {
                    return;
                }

                FlakinessAnalyzer analyzer = FlakinessAnalyzer.getInstance();
                FlakinessScore score = analyzer.analyzeTest(method);

                if (score.getOverallScore() >= 0.4) {
                    String message = String.format(
                        "Test has %s flakiness risk (%.0f%%)",
                        score.getRiskLevel(),
                        score.getOverallScore() * 100
                    );

                    PsiElement nameElement = method.getNameIdentifier();
                    if (nameElement != null) {
                        holder.registerProblem(
                            nameElement,
                            message,
                            ProblemHighlightType.WARNING
                        );
                    }
                }
            }

            private boolean isTestMethod(PsiMethod method) {
                PsiAnnotation[] annotations = method.getAnnotations();
                for (PsiAnnotation annotation : annotations) {
                    String qualifiedName = annotation.getQualifiedName();
                    if (qualifiedName != null && qualifiedName.contains("Test")) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}
