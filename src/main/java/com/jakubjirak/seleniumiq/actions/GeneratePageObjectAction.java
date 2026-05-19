/*
 * Copyright (c) 2025 Jakub Jirák. All rights reserved.
 * Licensed under the proprietary license. See LICENSE for details.
 */
package com.jakubjirak.seleniumiq.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Generates a Selenium Page Object Model class.
 *
 * <p>When a URL is provided the page is fetched with Jsoup and its interactive
 * elements (inputs, buttons, links, selects, textareas) are turned into
 * {@code @FindBy} annotated fields. Without a URL a well-structured template
 * is generated. The resulting {@code .java} file is written into the project
 * source tree and opened in the editor.
 */
public class GeneratePageObjectAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        // --- collect inputs ---------------------------------------------------
        String className = Messages.showInputDialog(
            project,
            "Page Object class name:",
            "Generate Page Object",
            Messages.getQuestionIcon(),
            "LoginPage",
            null
        );
        if (className == null || className.isBlank()) return;
        className = className.trim();

        String packageName = Messages.showInputDialog(
            project,
            "Package name:",
            "Generate Page Object — " + className,
            Messages.getQuestionIcon(),
            guessBasePackage(project),
            null
        );
        if (packageName == null || packageName.isBlank()) return;
        packageName = packageName.trim();

        String pageUrl = Messages.showInputDialog(
            project,
            "Page URL to analyse (leave blank to generate a template):",
            "Generate Page Object — " + className,
            Messages.getQuestionIcon(),
            "",
            null
        );
        if (pageUrl == null) return;
        pageUrl = pageUrl.trim();

        final String finalClassName   = className;
        final String finalPackage     = packageName;
        final String finalUrl         = pageUrl;

        // --- generate in background so EDT is not blocked ---------------------
        new Task.Backgroundable(project, "Generating Page Object: " + className, false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                indicator.setText("Analysing page...");

                List<FieldDef> fields = finalUrl.isEmpty()
                    ? templateFields()
                    : fetchFields(finalUrl, indicator);

                String source = buildSource(finalPackage, finalClassName, fields);

                ApplicationManager.getApplication().invokeLater(() ->
                    writeFile(project, finalPackage, finalClassName, source)
                );
            }
        }.queue();
    }

    // -------------------------------------------------------------------------
    // Field extraction
    // -------------------------------------------------------------------------

    private List<FieldDef> fetchFields(String url, ProgressIndicator indicator) {
        List<FieldDef> fields = new ArrayList<>();
        try {
            indicator.setText("Fetching " + url);
            Document doc = Jsoup.connect(url)
                .userAgent("SeleniumIQ-PageObjectGenerator/1.0")
                .timeout(10_000)
                .get();

            for (Element el : doc.select("input:not([type=hidden]), button, a[href], select, textarea")) {
                FieldDef field = toField(el);
                if (field != null) fields.add(field);
            }
        } catch (IOException ex) {
            // graceful fallback — the caller will use template fields
            fields.addAll(templateFields());
            fields.add(new FieldDef(
                "WebElement",
                "/* Could not fetch " + url + ": " + ex.getMessage() + " */",
                null,
                null,
                "// TODO: add @FindBy"
            ));
        }
        if (fields.isEmpty()) fields.addAll(templateFields());
        return fields;
    }

    private List<FieldDef> templateFields() {
        List<FieldDef> f = new ArrayList<>();
        f.add(new FieldDef("WebElement", "usernameField",  "id",       "username",   null));
        f.add(new FieldDef("WebElement", "passwordField",  "id",       "password",   null));
        f.add(new FieldDef("WebElement", "submitButton",   "cssSelector", "[type='submit']", null));
        f.add(new FieldDef("WebElement", "errorMessage",   "className", "error-msg", null));
        return f;
    }

    private FieldDef toField(Element el) {
        String tag  = el.tagName().toLowerCase(Locale.ROOT);
        String name = deriveFieldName(el, tag);
        if (name == null) return null;

        // --- choose the most stable locator -----------------------------------
        if (!el.attr("data-testid").isBlank()) {
            return new FieldDef("WebElement", name, "cssSelector",
                "[data-testid='" + el.attr("data-testid") + "']", null);
        }
        if (!el.id().isBlank() && !isDynamic(el.id())) {
            return new FieldDef("WebElement", name, "id", el.id(), null);
        }
        if (!el.attr("name").isBlank()) {
            return new FieldDef("WebElement", name, "name", el.attr("name"), null);
        }
        if (!el.attr("aria-label").isBlank()) {
            return new FieldDef("WebElement", name, "cssSelector",
                "[aria-label='" + el.attr("aria-label") + "']", null);
        }
        // compound CSS fallback
        StringBuilder css = new StringBuilder(tag);
        if (!el.attr("type").isBlank()) css.append("[type='").append(el.attr("type")).append("']");
        if (!el.attr("placeholder").isBlank())
            css.append("[placeholder='").append(el.attr("placeholder")).append("']");
        if (css.length() > tag.length()) {
            return new FieldDef("WebElement", name, "cssSelector", css.toString(), null);
        }
        return null; // element has no stable attribute — skip
    }

    private String deriveFieldName(Element el, String tag) {
        // prefer data-testid, then aria-label, then id, then placeholder/name, then type
        for (String attr : new String[]{"data-testid", "aria-label", "id", "name", "placeholder"}) {
            String val = el.attr(attr);
            if (!val.isBlank() && !isDynamic(val)) {
                return toCamelCase(val) + typeSuffix(tag, el.attr("type"));
            }
        }
        String text = el.text().trim();
        if (!text.isBlank() && text.length() < 30) {
            return toCamelCase(text) + typeSuffix(tag, el.attr("type"));
        }
        return null;
    }

    private String typeSuffix(String tag, String type) {
        return switch (tag) {
            case "input"    -> switch (type.toLowerCase(Locale.ROOT)) {
                case "submit", "button" -> "Button";
                case "checkbox"         -> "Checkbox";
                case "radio"            -> "Radio";
                default                 -> "Field";
            };
            case "button"   -> "Button";
            case "select"   -> "Dropdown";
            case "textarea" -> "TextArea";
            case "a"        -> "Link";
            default         -> "Element";
        };
    }

    private boolean isDynamic(String val) {
        return val.matches(".*\\d{5,}.*")
            || val.matches(".*[0-9a-f]{8}-[0-9a-f]{4}.*")
            || val.contains("generated")
            || val.contains("random");
    }

    // -------------------------------------------------------------------------
    // Source generation
    // -------------------------------------------------------------------------

    private String buildSource(String pkg, String cls, List<FieldDef> fields) {
        StringBuilder sb = new StringBuilder();

        sb.append("/*\n");
        sb.append(" * Generated by SeleniumIQ — AI-Powered Test Automation\n");
        sb.append(" * https://github.com/JirakJ/selenium-iq-plugin\n");
        sb.append(" */\n");
        sb.append("package ").append(pkg).append(";\n\n");
        sb.append("import org.openqa.selenium.WebDriver;\n");
        sb.append("import org.openqa.selenium.WebElement;\n");
        sb.append("import org.openqa.selenium.support.FindBy;\n");
        sb.append("import org.openqa.selenium.support.PageFactory;\n\n");

        sb.append("/**\n");
        sb.append(" * Page Object for ").append(cls).append(".\n");
        sb.append(" *\n");
        sb.append(" * <p>Generated by SeleniumIQ. Review all {@code @FindBy} locators before committing.\n");
        sb.append(" * Replace any {@code // TODO} placeholders with stable locators.\n");
        sb.append(" */\n");
        sb.append("public class ").append(cls).append(" {\n\n");

        for (FieldDef f : fields) {
            if (f.comment() != null) {
                sb.append("    ").append(f.comment()).append("\n");
                sb.append("    private ").append(f.type()).append(" ").append(f.fieldName()).append(";\n\n");
                continue;
            }
            sb.append("    @FindBy(").append(f.how()).append(" = \"").append(f.using()).append("\")\n");
            sb.append("    private ").append(f.type()).append(" ").append(f.fieldName()).append(";\n\n");
        }

        sb.append("    public ").append(cls).append("(WebDriver driver) {\n");
        sb.append("        PageFactory.initElements(driver, this);\n");
        sb.append("    }\n\n");

        // accessor methods
        for (FieldDef f : fields) {
            if (f.comment() != null || f.fieldName().startsWith("/*")) continue;
            String cap = Character.toUpperCase(f.fieldName().charAt(0)) + f.fieldName().substring(1);
            sb.append("    /** @return {@link #").append(f.fieldName()).append("} */\n");
            sb.append("    public ").append(f.type()).append(" get").append(cap).append("() {\n");
            sb.append("        return ").append(f.fieldName()).append(";\n");
            sb.append("    }\n\n");
        }

        sb.append("}\n");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // File I/O
    // -------------------------------------------------------------------------

    private void writeFile(Project project, String pkg, String cls, String source) {
        Path dir = resolvePackageDir(project, pkg);
        if (dir == null) {
            Messages.showErrorDialog(
                project,
                "Could not locate a source root for package \"" + pkg + "\".\n" +
                "Please create the package directory manually and paste the generated class.",
                "Write Failed"
            );
            Messages.showInfoMessage(project, source, cls + ".java — Generated Source");
            return;
        }

        try {
            Files.createDirectories(dir);
            Path file = dir.resolve(cls + ".java");
            Files.writeString(file, source, StandardCharsets.UTF_8);

            // Refresh VFS and open the file
            VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(file);
            if (vf != null) {
                FileEditorManager.getInstance(project).openFile(vf, true);
                Messages.showInfoMessage(
                    project,
                    "Page Object class created:\n" + file + "\n\n" +
                    "Review the @FindBy locators and update any TODO placeholders.",
                    "Page Object Generated"
                );
            }
        } catch (IOException ex) {
            Messages.showErrorDialog(project, "Failed to write file: " + ex.getMessage(), "Write Error");
        }
    }

    /**
     * Finds the first Java source root in the project and appends the package path.
     * Returns {@code null} when no source root is found.
     */
    private Path resolvePackageDir(Project project, String pkg) {
        VirtualFile[] sourceRoots = ProjectRootManager.getInstance(project).getContentSourceRoots();
        for (VirtualFile root : sourceRoots) {
            if (root.getPath().contains("test")) continue; // prefer main source root
            String pkgPath = pkg.replace('.', '/');
            return Path.of(root.getPath(), pkgPath);
        }
        if (sourceRoots.length > 0) {
            return Path.of(sourceRoots[0].getPath(), pkg.replace('.', '/'));
        }
        return null;
    }

    private String guessBasePackage(Project project) {
        VirtualFile[] roots = ProjectRootManager.getInstance(project).getContentSourceRoots();
        for (VirtualFile root : roots) {
            if (!root.getPath().contains("test")) {
                // try to find an existing package from a source file
                VirtualFile found = findFirstJavaPackage(root);
                if (found != null) return found.getPath()
                    .replace(root.getPath() + "/", "")
                    .replace("/", ".")
                    .replace(".java", "");
            }
        }
        return "com.example.pages";
    }

    private VirtualFile findFirstJavaPackage(VirtualFile dir) {
        for (VirtualFile child : dir.getChildren()) {
            if (child.isDirectory()) {
                VirtualFile nested = findFirstJavaPackage(child);
                if (nested != null) return nested;
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static String toCamelCase(String raw) {
        String[] parts = raw.replaceAll("[^a-zA-Z0-9]+", " ").trim().split("\\s+");
        if (parts.length == 0) return "element";
        StringBuilder sb = new StringBuilder(parts[0].toLowerCase(Locale.ROOT));
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].isBlank()) {
                sb.append(Character.toUpperCase(parts[i].charAt(0)));
                sb.append(parts[i].substring(1).toLowerCase(Locale.ROOT));
            }
        }
        // ensure valid Java identifier
        if (Character.isDigit(sb.charAt(0))) sb.insert(0, "el");
        return sb.toString();
    }

    /**
     * Value object representing one field in the generated Page Object.
     *
     * @param type      Java type, e.g. {@code "WebElement"}
     * @param fieldName camelCase field name
     * @param how       {@code @FindBy} attribute name, e.g. {@code "id"}, {@code "cssSelector"}
     * @param using     locator value
     * @param comment   if non-null, replaces the @FindBy with a plain comment line
     */
    private record FieldDef(String type, String fieldName, String how, String using, String comment) {}

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(e.getProject() != null);
    }
}

