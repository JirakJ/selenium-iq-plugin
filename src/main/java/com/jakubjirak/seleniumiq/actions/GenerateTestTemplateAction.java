package com.jakubjirak.seleniumiq.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.datatransfer.StringSelection;

/**
 * Generates a Selenium test class skeleton (WebDriver setup/teardown, explicit wait, one sample test)
 * for JUnit 5, JUnit 4 or TestNG and copies it to the clipboard.
 */
public class GenerateTestTemplateAction extends AnAction {

    static final String[] FRAMEWORKS = {"JUnit 5", "JUnit 4", "TestNG"};

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        int choice = Messages.showDialog(project, "Select test framework for template:", "Generate Test Template",
                FRAMEWORKS, 0, Messages.getQuestionIcon());
        if (choice < 0 || choice >= FRAMEWORKS.length) {
            return;
        }
        String className = Messages.showInputDialog(project, "Enter test class name:",
                "Generate " + FRAMEWORKS[choice] + " Test", Messages.getQuestionIcon());
        String source = buildTemplate(FRAMEWORKS[choice], className);
        if (source == null) {
            if (className != null) {
                Messages.showErrorDialog(project, "'" + className + "' is not a valid Java class name.", "Generate Test Template");
            }
            return;
        }
        CopyPasteManager.getInstance().setContents(new StringSelection(source));
        Messages.showInfoMessage(project,
                FRAMEWORKS[choice] + " test class " + className.trim() + " was copied to the clipboard.",
                "Test Template Ready");
    }

    /**
     * Java source of the test class, or {@code null} for an unknown framework or an invalid class name.
     */
    @Nullable
    static String buildTemplate(@NotNull String framework, @Nullable String className) {
        if (className == null || !className.trim().matches("[A-Za-z_$][A-Za-z0-9_$]*")) {
            return null;
        }
        String cls = className.trim();
        switch (framework) {
            case "JUnit 5":
                return source(cls, "import org.junit.jupiter.api.AfterEach;\nimport org.junit.jupiter.api.BeforeEach;\n"
                        + "import org.junit.jupiter.api.Test;\n\nimport static org.junit.jupiter.api.Assertions.assertTrue;\n",
                        "@BeforeEach", "@AfterEach", "@Test", "assertTrue(");
            case "JUnit 4":
                return source(cls, "import org.junit.After;\nimport org.junit.Before;\nimport org.junit.Test;\n\n"
                        + "import static org.junit.Assert.assertTrue;\n",
                        "@Before", "@After", "@Test", "assertTrue(");
            case "TestNG":
                return source(cls, "import org.testng.annotations.AfterMethod;\nimport org.testng.annotations.BeforeMethod;\n"
                        + "import org.testng.annotations.Test;\n\nimport static org.testng.Assert.assertTrue;\n",
                        "@BeforeMethod", "@AfterMethod", "@Test", "assertTrue(");
            default:
                return null;
        }
    }

    private static String source(String cls, String frameworkImports, String before, String after, String test,
                                 String assertCall) {
        return "import org.openqa.selenium.By;\n"
                + "import org.openqa.selenium.WebDriver;\n"
                + "import org.openqa.selenium.chrome.ChromeDriver;\n"
                + "import org.openqa.selenium.support.ui.ExpectedConditions;\n"
                + "import org.openqa.selenium.support.ui.WebDriverWait;\n"
                + frameworkImports
                + "\nimport java.time.Duration;\n\n"
                + "public class " + cls + " {\n\n"
                + "    private WebDriver driver;\n"
                + "    private WebDriverWait wait;\n\n"
                + "    " + before + "\n"
                + "    public void setUp() {\n"
                + "        driver = new ChromeDriver();\n"
                + "        wait = new WebDriverWait(driver, Duration.ofSeconds(10));\n"
                + "    }\n\n"
                + "    " + after + "\n"
                + "    public void tearDown() {\n"
                + "        if (driver != null) {\n"
                + "            driver.quit();\n"
                + "        }\n"
                + "    }\n\n"
                + "    " + test + "\n"
                + "    public void pageLoads() {\n"
                + "        driver.get(\"https://example.com\");\n"
                + "        // Prefer data-testid / id locators and explicit waits instead of fixed sleeps\n"
                + "        " + assertCall + "wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName(\"h1\"))).isDisplayed());\n"
                + "    }\n"
                + "}\n";
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(e.getProject() != null);
    }
}
