package com.jakubjirak.seleniumiq.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.jakubjirak.seleniumiq.models.FlakinessScore;
import com.jakubjirak.seleniumiq.services.FlakinessAnalyzer;
import org.jetbrains.annotations.NotNull;

public class AnalyzeFlakinessAction extends AnAction {

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

        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) {
            return;
        }

        FlakinessAnalyzer analyzer = FlakinessAnalyzer.getInstance();
        
        PsiElement element = e.getData(CommonDataKeys.PSI_ELEMENT);
        if (element != null) {
            PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
            if (method != null) {
                analyzeMethod(project, method, analyzer);
                return;
            }
        }

        analyzeFile(project, psiFile, analyzer);
    }

    private void analyzeMethod(Project project, PsiMethod method, FlakinessAnalyzer analyzer) {
        FlakinessScore score = analyzer.analyzeTest(method);
        String report = analyzer.generateFlakinessReport(score);
        
        Messages.showMessageDialog(
            project,
            report,
            "Flakiness Analysis: " + method.getName(),
            Messages.getInformationIcon()
        );
    }

    private void analyzeFile(Project project, PsiFile file, FlakinessAnalyzer analyzer) {
        FlakinessScore score = analyzer.analyzeTestFile(file);
        String report = analyzer.generateFlakinessReport(score);
        
        Messages.showMessageDialog(
            project,
            report,
            "Flakiness Analysis: " + file.getName(),
            Messages.getInformationIcon()
        );
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        e.getPresentation().setEnabledAndVisible(project != null && psiFile != null);
    }
}
