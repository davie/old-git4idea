package com.assembla.git;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;

public class GitContentRevision implements ContentRevision
{
	private FilePath file;
	private GitRevisionNumber revision;
	private byte[] content;
	private Project project;

	public GitContentRevision( FilePath filePath, GitRevisionNumber revision, Project project )
	{
		this.project = project;
		this.file = filePath;
		this.revision = revision;
	}

	@Nullable
	public String getContent() throws VcsException
	{
		com.assembla.git.commands.GitCommand command = new com.assembla.git.commands.GitCommand(
                project,
                GitVcsSettings.getInstance( project ),
                GitUtil.getVcsRoot(project, file));

        if( content == null )
			content = command.cat( file.getPath(), revision.getNumber() );
		return com.assembla.git.commands.GitCommand.convertStreamToString( new ByteArrayInputStream( content ) );
	}

	@NotNull
	public FilePath getFile()
	{
		return file;
	}

	@NotNull
	public VcsRevisionNumber getRevisionNumber()
	{
		return VcsRevisionNumber.NULL;
	}
}
