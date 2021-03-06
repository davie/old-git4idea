package com.assembla.git;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.assembla.git.commands.GitCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;

public class GitContentRevision implements ContentRevision
{
	private final FilePath file;
	private final GitRevisionNumber revision;
    private final Project project;
    private byte[] content;

    public GitContentRevision( FilePath filePath, GitRevisionNumber revision, Project project )
	{
		this.project = project;
		this.file = filePath;
		this.revision = revision;
	}

	@Nullable
	public String getContent() throws VcsException
	{
		GitCommand command = new GitCommand(
                project,
                GitVcsSettings.getInstance( project ),
                GitUtil.getVcsRoot(project, file));

        if( content == null )
			content = command.cat( file.getPath(), revision.getVersion() );
		return GitCommand.convertStreamToString( new ByteArrayInputStream( content ) );
	}

	@NotNull
	public FilePath getFile()
	{
		return file;
	}

	@NotNull
	public VcsRevisionNumber getRevisionNumber()
	{
		return revision;
	}
}
