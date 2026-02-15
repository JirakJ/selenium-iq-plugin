package com.jakubjirak.seleniumiq.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class SeleniumCompletionContributor extends CompletionContributor {

    public SeleniumCompletionContributor() {
        extend(CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            new CompletionProvider<>() {
                @Override
                protected void addCompletions(@NotNull CompletionParameters parameters,
                                            @NotNull ProcessingContext context,
                                            @NotNull CompletionResultSet result) {
                    
                    // Add Selenium WebDriver API completions
                    result.addElement(LookupElementBuilder.create("driver.findElement(By.id(\"\"))")
                        .withPresentableText("findElement by ID")
                        .withTailText(" (SeleniumIQ)", true)
                        .withTypeText("WebElement"));
                    
                    result.addElement(LookupElementBuilder.create("driver.findElement(By.cssSelector(\"\"))")
                        .withPresentableText("findElement by CSS")
                        .withTailText(" (SeleniumIQ)", true)
                        .withTypeText("WebElement"));
                    
                    result.addElement(LookupElementBuilder.create("driver.findElement(By.xpath(\"\"))")
                        .withPresentableText("findElement by XPath")
                        .withTailText(" (SeleniumIQ)", true)
                        .withTypeText("WebElement"));
                    
                    result.addElement(LookupElementBuilder.create("new WebDriverWait(driver, Duration.ofSeconds(10))")
                        .withPresentableText("WebDriverWait")
                        .withTailText(" (SeleniumIQ)", true)
                        .withTypeText("WebDriverWait"));
                }
            }
        );
    }
}
