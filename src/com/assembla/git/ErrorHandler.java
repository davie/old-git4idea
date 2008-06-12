package com.assembla.git;

import com.intellij.openapi.project.Project;

public class ErrorHandler implements IErrorHandler {
    private Project project;

    public ErrorHandler(Project project) {
        this.project = project;
    }

    public void displayErrorMessage(String message) {
        GitVcs.getInstance(project).showMessages(message);
    }
}
