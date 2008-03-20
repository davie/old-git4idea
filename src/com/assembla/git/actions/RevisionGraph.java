package com.assembla.git.actions;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RevisionGraph extends BasicAction
{
	public void perform( @NotNull Project project, com.assembla.git.GitVcs vcs, @NotNull List<VcsException> exceptions,
	                     @NotNull VirtualFile[] affectedFiles ) throws VcsException
	{
        String url = com.assembla.git.GitFileSystem.getRevisionGraphUrl(affectedFiles[0].getPath());

        final VirtualFile vFile = VirtualFileManager.getInstance().findFileByUrl(url);

        FileEditorManager.getInstance(project).openFile(vFile, true);
    }

	@NotNull
	protected String getActionName( @NotNull AbstractVcs abstractvcs )
	{
		return "Tag";
	}

	protected boolean isEnabled( @NotNull Project project, @NotNull com.assembla.git.GitVcs vcs, @NotNull VirtualFile... vFiles )
	{
		return vFiles.length == 1 && FileStatusManager.getInstance(project).getStatus(vFiles[0]) != FileStatus.UNKNOWN;
	}
}
