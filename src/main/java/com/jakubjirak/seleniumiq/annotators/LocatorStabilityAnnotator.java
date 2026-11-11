package com.jakubjirak.seleniumiq.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpressionList;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiReferenceExpression;
import com.jakubjirak.seleniumiq.services.AILocatorService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

/**
 * Shows the stability score of string literals passed to Selenium {@code By.xxx(...)} factories.
 * Other string literals are ignored (the previous heuristic flagged any string containing '.', '/' or '-').
 */
public class LocatorStabilityAnnotator implements Annotator {

    private static final Map<String, String> BY_METHOD_TO_TYPE = Map.of(
            "id", "id",
            "name", "name",
            "className", "className",
            "cssSelector", "cssSelector",
            "xpath", "xpath",
            "linkText", "linkText",
            "partialLinkText", "partialLinkText",
            "tagName", "tagName");

    static final double LOW_STABILITY = 0.4;
    static final double HIGH_STABILITY = 0.8;

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (!(element instanceof PsiLiteralExpression literal) || !(literal.getValue() instanceof String locator)) {
            return;
        }
        if (!(literal.getParent() instanceof PsiExpressionList args)
                || !(args.getParent() instanceof PsiMethodCallExpression call)) {
            return;
        }
        PsiReferenceExpression ref = call.getMethodExpression();
        PsiElement qualifier = ref.getQualifier();
        String type = locatorTypeForCall(qualifier == null ? null : qualifier.getText(), ref.getReferenceName());
        if (type == null) {
            return;
        }
        String message = message(AILocatorService.getInstance().calculateStabilityScore(locator, type));
        if (message == null) {
            return;
        }
        HighlightSeverity severity = message.startsWith("Low") ? HighlightSeverity.WARNING : HighlightSeverity.INFORMATION;
        holder.newAnnotation(severity, message).range(element).create();
    }

    /** Locator type for a {@code By.<method>(...)} call, or {@code null} when the call is not a Selenium locator factory. */
    @Nullable
    static String locatorTypeForCall(@Nullable String qualifier, @Nullable String method) {
        if (method == null || qualifier == null) {
            return null;
        }
        boolean isBy = "By".equals(qualifier) || qualifier.endsWith(".By");
        return isBy ? BY_METHOD_TO_TYPE.get(method) : null;
    }

    /** Annotation text for a stability score, or {@code null} for the unremarkable middle range. */
    @Nullable
    static String message(double stability) {
        long percent = Math.round(stability * 100);
        if (stability < LOW_STABILITY) {
            return String.format(Locale.ROOT, "Low stability locator (%d%%) - consider SeleniumIQ > Generate Locator", percent);
        }
        if (stability >= HIGH_STABILITY) {
            return String.format(Locale.ROOT, "Excellent locator stability (%d%%)", percent);
        }
        return null;
    }
}
