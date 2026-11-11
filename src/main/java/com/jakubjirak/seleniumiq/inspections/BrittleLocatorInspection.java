package com.jakubjirak.seleniumiq.inspections;

import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class BrittleLocatorInspection extends AbstractBaseJavaLocalInspectionTool {

    private static final Pattern ABSOLUTE_XPATH = Pattern.compile("/html/body");
    private static final Pattern DYNAMIC_ID = Pattern.compile("\\d{5,}");
    /** {@code #id} selectors or {@code @id} XPath attributes. */
    private static final Pattern ID_REFERENCE = Pattern.compile("(^#|@id\\b|\\[id[=~^$*]?)");

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

                String problem = brittleReason(locator);
                if (problem != null) {
                    holder.registerProblem(expression, problem,
                            problem.startsWith("Absolute") ? ProblemHighlightType.WARNING : ProblemHighlightType.WEAK_WARNING);
                }
            }
        };
    }

    /** Why {@code locator} is brittle, or {@code null} when it looks fine. */
    public static String brittleReason(String locator) {
        if (ABSOLUTE_XPATH.matcher(locator).find()) {
            return "Absolute XPath is brittle - consider using relative XPath or CSS selectors";
        }
        if (DYNAMIC_ID.matcher(locator).find() && ID_REFERENCE.matcher(locator).find()) {
            return "ID contains a long number - may be dynamically generated";
        }
        return null;
    }
}
