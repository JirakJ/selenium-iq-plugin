/*
 * Copyright (c) 2025 Jakub Jirák..
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.seleniumiq.toolwindow;

import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBUI;
import com.jakubjirak.seleniumiq.actions.AnalyzeFlakinessAction;
import com.jakubjirak.seleniumiq.actions.GenerateLocatorAction;
import com.jakubjirak.seleniumiq.actions.GeneratePageObjectAction;
import com.jakubjirak.seleniumiq.actions.HealLocatorsAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class SeleniumIQToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        SeleniumIQToolWindowContent toolWindowContent = new SeleniumIQToolWindowContent(project);
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(toolWindowContent.getContentPanel(), "", false);
        toolWindow.getContentManager().addContent(content);
    }

    public static class SeleniumIQToolWindowContent {
        private final JPanel contentPanel;

        public SeleniumIQToolWindowContent(Project project) {
            contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setBorder(JBUI.Borders.empty(12));

            JBLabel titleLabel = new JBLabel("SeleniumIQ Dashboard");
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(titleLabel);
            contentPanel.add(Box.createVerticalStrut(4));

            JSeparator separator = new JSeparator();
            separator.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(separator);
            contentPanel.add(Box.createVerticalStrut(12));

            addSectionLabel("Locator Tools");

            addButton("Generate Locator",
                    "Suggest stable Selenium locators ranked by stability (Ctrl+Alt+L)",
                    e -> {
                        Editor editor = getSelectedEditor(project);
                        if (editor == null) {
                            showNoEditorMessage(project);
                            return;
                        }
                        DataContext ctx = SimpleDataContext.builder()
                                .add(CommonDataKeys.PROJECT, project)
                                .add(CommonDataKeys.EDITOR, editor)
                                .build();
                        invokeAction(new GenerateLocatorAction(), ctx);
                    });

            addButton("Heal Broken Locators",
                    "Select a locator in the editor, then click to get stable replacements",
                    e -> {
                        Editor editor = getSelectedEditor(project);
                        if (editor == null) {
                            showNoEditorMessage(project);
                            return;
                        }
                        DataContext ctx = SimpleDataContext.builder()
                                .add(CommonDataKeys.PROJECT, project)
                                .add(CommonDataKeys.EDITOR, editor)
                                .build();
                        invokeAction(new HealLocatorsAction(), ctx);
                    });

            contentPanel.add(Box.createVerticalStrut(8));
            addSectionLabel("Analysis");

            addButton("Analyze Flakiness",
                    "Detect flaky test patterns in the current file or method",
                    e -> {
                        Editor editor = getSelectedEditor(project);
                        if (editor == null) {
                            showNoEditorMessage(project);
                            return;
                        }
                        PsiFile psiFile = PsiDocumentManager.getInstance(project)
                                .getPsiFile(editor.getDocument());
                        if (psiFile == null) {
                            Messages.showInfoMessage(project,
                                    "The current file could not be parsed. Please open a Java test file.",
                                    "Analyze Flakiness");
                            return;
                        }
                        PsiElement element = psiFile.findElementAt(editor.getCaretModel().getOffset());
                        SimpleDataContext.Builder builder = SimpleDataContext.builder()
                                .add(CommonDataKeys.PROJECT, project)
                                .add(CommonDataKeys.PSI_FILE, psiFile)
                                .add(CommonDataKeys.EDITOR, editor);
                        if (element != null) {
                            builder.add(CommonDataKeys.PSI_ELEMENT, element);
                        }
                        invokeAction(new AnalyzeFlakinessAction(), builder.build());
                    });

            contentPanel.add(Box.createVerticalStrut(8));
            addSectionLabel("Generation");

            addButton("Generate Page Object",
                    "Create a Page Object Model class from a URL",
                    e -> {
                        DataContext ctx = SimpleDataContext.builder()
                                .add(CommonDataKeys.PROJECT, project)
                                .build();
                        invokeAction(new GeneratePageObjectAction(), ctx);
                    });

            contentPanel.add(Box.createVerticalGlue());
        }

        private void addSectionLabel(String text) {
            JBLabel label = new JBLabel(text.toUpperCase());
            label.setFont(label.getFont().deriveFont(Font.BOLD, 10f));
            label.setForeground(JBColor.GRAY);
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(label);
            contentPanel.add(Box.createVerticalStrut(4));
        }

        private void addButton(String text, String tooltip, ActionListener listener) {
            JButton button = new JButton(text);
            button.setToolTipText(tooltip);
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
            button.addActionListener(listener);
            contentPanel.add(button);
            contentPanel.add(Box.createVerticalStrut(6));
        }

        private void invokeAction(com.intellij.openapi.actionSystem.AnAction action, DataContext dataContext) {
            AnActionEvent event = AnActionEvent.createEvent(
                    dataContext, null, ActionPlaces.TOOLWINDOW_CONTENT,
                    com.intellij.openapi.actionSystem.ActionUiKind.NONE, null);
            com.intellij.openapi.actionSystem.ex.ActionUtil.performActionDumbAwareWithCallbacks(action, event);
        }

        private Editor getSelectedEditor(Project project) {
            return FileEditorManager.getInstance(project).getSelectedTextEditor();
        }

        private void showNoEditorMessage(Project project) {
            Messages.showInfoMessage(project,
                    "Please open a Selenium test file in the editor first.",
                    "SeleniumIQ");
        }

        public JPanel getContentPanel() {
            return contentPanel;
        }
    }
}
