package com.jakubjirak.seleniumiq.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

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
            
            contentPanel.add(new JLabel("SeleniumIQ Dashboard"));
            contentPanel.add(Box.createVerticalStrut(20));
            
            JButton generateLocatorBtn = new JButton("Generate AI Locator");
            generateLocatorBtn.addActionListener(e -> {
                // Trigger action
            });
            contentPanel.add(generateLocatorBtn);
            
            contentPanel.add(Box.createVerticalStrut(10));
            
            JButton healLocatorsBtn = new JButton("Heal Broken Locators");
            healLocatorsBtn.addActionListener(e -> {
                // Trigger action
            });
            contentPanel.add(healLocatorsBtn);
            
            contentPanel.add(Box.createVerticalStrut(10));
            
            JButton analyzeBtn = new JButton("Analyze Flakiness");
            analyzeBtn.addActionListener(e -> {
                // Trigger action
            });
            contentPanel.add(analyzeBtn);
        }

        public JPanel getContentPanel() {
            return contentPanel;
        }
    }
}
