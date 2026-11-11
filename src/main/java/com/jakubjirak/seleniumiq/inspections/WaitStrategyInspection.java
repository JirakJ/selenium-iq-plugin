package com.jakubjirak.seleniumiq.inspections;

import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

public class WaitStrategyInspection extends AbstractBaseJavaLocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                super.visitMethodCallExpression(expression);

                PsiMethod method = expression.resolveMethod();
                if (method == null) {
                    return;
                }

                String methodName = method.getName();
                PsiClass containingClass = method.getContainingClass();
                
                if (containingClass != null && 
                    "Thread".equals(containingClass.getName()) && 
                    "sleep".equals(methodName)) {
                    
                    holder.registerProblem(
                        expression,
                        "Thread.sleep() creates flaky tests - use explicit waits (WebDriverWait)",
                        ProblemHighlightType.WARNING
                    );
                }
            }
        };
    }
}
