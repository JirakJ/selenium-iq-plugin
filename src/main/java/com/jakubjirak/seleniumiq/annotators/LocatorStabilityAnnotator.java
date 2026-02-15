package com.jakubjirak.seleniumiq.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.jakubjirak.seleniumiq.services.AILocatorService;
import org.jetbrains.annotations.NotNull;

public class LocatorStabilityAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (!(element instanceof PsiLiteralExpression)) {
            return;
        }

        PsiLiteralExpression literal = (PsiLiteralExpression) element;
        Object value = literal.getValue();
        
        if (!(value instanceof String)) {
            return;
        }

        String locator = (String) value;
        
        // Check if this looks like a Selenium locator
        if (!isSeleniumLocator(locator)) {
            return;
        }

        AILocatorService aiService = AILocatorService.getInstance();
        String locatorType = detectLocatorType(locator);
        double stability = aiService.calculateStabilityScore(locator, locatorType);

        if (stability < 0.4) {
            holder.newAnnotation(HighlightSeverity.WARNING, 
                String.format("Low stability locator (%.0f%%) - consider using SeleniumIQ AI generator", stability * 100))
                .range(element)
                .create();
        } else if (stability >= 0.8) {
            holder.newAnnotation(HighlightSeverity.INFORMATION, 
                String.format("Excellent locator stability (%.0f%%)", stability * 100))
                .range(element)
                .create();
        }
    }

    private boolean isSeleniumLocator(String value) {
        return value.contains("/") || 
               value.contains("#") || 
               value.contains(".") ||
               value.contains("[") ||
               value.matches(".*-.*");
    }

    private String detectLocatorType(String locator) {
        if (locator.startsWith("//") || locator.startsWith("/html")) {
            return "xpath";
        } else if (locator.startsWith("#") || locator.contains("[")) {
            return "cssSelector";
        } else {
            return "id";
        }
    }
}
