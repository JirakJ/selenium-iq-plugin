package com.jakubjirak.seleniumiq.services;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

@Service(Service.Level.PROJECT)
public final class SeleniumIQProjectService {
    private final Project project;

    public SeleniumIQProjectService(Project project) {
        this.project = project;
    }

    public static SeleniumIQProjectService getInstance(Project project) {
        return project.getService(SeleniumIQProjectService.class);
    }

    public Project getProject() {
        return project;
    }
}
