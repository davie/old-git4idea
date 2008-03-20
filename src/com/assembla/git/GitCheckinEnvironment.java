package com.assembla.git;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.checkin.CheckinEnvironment;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GitCheckinEnvironment implements CheckinEnvironment
{
	private Project project;
	private final GitVcsSettings settings;

	public GitCheckinEnvironment( Project project, GitVcsSettings settings )
	{
		this.project = project;
		this.settings = settings;
	}

	@Nullable
	public RefreshableOnComponent createAdditionalOptionsPanel( CheckinProjectPanel panel )
	{
		return null;
	}

	@Nullable
	public String getDefaultMessageFor( FilePath[] filesToCheckin )
	{
		return "Commit";
	}

	public String prepareCheckinMessage( String text )
	{
		return null;
	}

	@Nullable
	@NonNls
	public String getHelpId()
	{
		return null;
	}

	public String getCheckinOperationName()
	{
		return "Commit";
	}

	public boolean showCheckinDialogInAnyCase()
	{
		return false;
	}

	public List<VcsException> commit( List<Change> changes, String preparedComment )
	{
		List<VcsException> exceptions = new ArrayList<VcsException>();

		Map<VirtualFile, List<Change>> sortedChanges = sortChangesByVcsRoot( changes );

		for( VirtualFile root : sortedChanges.keySet() )
		{
			com.assembla.git.commands.GitCommand command = new com.assembla.git.commands.GitCommand( project, settings, root );
			Set<String> paths = new HashSet<String>();
			for( Change change : changes )
			{
				if( change.getFileStatus().equals( FileStatus.MODIFIED ) )
					paths.add( change.getAfterRevision().getFile().getPath() );
				else if( change.getFileStatus().equals( FileStatus.DELETED ) )
					paths.add( change.getBeforeRevision().getFile().getPath() );
				else if( change.getFileStatus().equals( FileStatus.ADDED ) )
					paths.add( change.getAfterRevision().getFile().getPath() );
			}
			try
			{
				command.commit( paths, preparedComment );
			}
			catch( VcsException e )
			{
				exceptions.add( e );
			}
		}


		return exceptions;
	}

	private Map<VirtualFile, List<Change>> sortChangesByVcsRoot( List<Change> changes )
	{
		Map<VirtualFile, List<Change>> result = new HashMap<VirtualFile, List<Change>>();

		for( Change change : changes )
		{
			final ContentRevision afterRevision = change.getAfterRevision();
			final ContentRevision beforeRevision = change.getBeforeRevision();

			final FilePath filePath = afterRevision != null ? afterRevision.getFile() : beforeRevision.getFile();
			final VirtualFile vcsRoot = GitUtil.getVcsRoot( project, filePath );

			List<Change> changeList = result.get( vcsRoot );
			if( changeList == null )
			{
				changeList = new ArrayList<Change>();
				result.put( vcsRoot, changeList );
			}
			changeList.add( change );
		}

		return result;
	}

	public List<VcsException> scheduleMissingFileForDeletion( List<FilePath> files )
	{
		return null;
	}

	public List<VcsException> scheduleUnversionedFilesForAddition( List<VirtualFile> files )
	{
		try
		{
			com.assembla.git.actions.Add.addFiles( project, files.toArray( new VirtualFile[files.size()] ) );
			return Collections.emptyList();
		}
		catch( VcsException e )
		{
			return Collections.singletonList( e );
		}
	}
}
