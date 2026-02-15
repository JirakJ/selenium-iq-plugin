package com.jakubjirak.seleniumiq.inspections;

import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class BrittleLocatorInspection extends AbstractBaseJavaLocalInspectionTool {

    private static final Pattern ABSOLUTE_XPATH = Pattern.compile("/html/body");
    private static final Pattern DYNAMIC_ID = Pattern.compile("\\d{5,}");

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitLiteralExpression(PsiLiteralExpression expression) {
                super.visitLiteralExpression(expression);

                Object value = expression.getValue();
                if (!(value instanceof String)) {
                    return;
                }

                String locator = (String) value;

                if (ABSOLUTE_XPATH.matcher(locator).find()) {
                    holder.registerProblem(
                        expression,
                        "Absolute XPath is brittle - consider using relative XPath or CSS selectors",
                        ProblemHighlightType.WARNING
                    );
                } else if (DYNAMIC_ID.matcher(locator).find() && locator.contains("id")) {
                    holder.registerProblem(
                        expression,
                        "ID contains numbers - may be dynamically generated",
                        ProblemHighlightType.WEAK_WARNING
                    );
                }
            }
        };
    }
}
