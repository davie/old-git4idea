package com.assembla.git;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.diff.DiffProvider;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.Nullable;

public class GitDiffProvider implements DiffProvider
{
	private Project project;

	public GitDiffProvider( Project project )
	{
		this.project = project;
	}

	@Nullable
	public VcsRevisionNumber getCurrentRevision( VirtualFile file )
	{
		return new GitRevisionNumber( GitRevisionNumber.TIP );
	}

	@Nullable
	public VcsRevisionNumber getLastRevision( VirtualFile virtualFile )
	{
		return new GitRevisionNumber( GitRevisionNumber.TIP );
	}

	@Nullable
	public ContentRevision createFileContent( VcsRevisionNumber revisionNumber, VirtualFile selectedFile )
	{
		return new GitContentRevision(
				VcsUtil.getFilePath( selectedFile.getPath() ), new GitRevisionNumber( GitRevisionNumber.TIP ), project );
	}
}
