package com.assembla.git;

import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.vcsUtil.VcsUtil;

import java.util.Set;
import java.util.LinkedHashSet;

public class GitChangeProvider implements ChangeProvider
{
	private static final Logger LOG = Logger.getInstance(GitChangeProvider.class.getName() );

	private Project project;
	private final GitVcsSettings settings;

	private final Set<VirtualFile> vcsRoots = new LinkedHashSet<VirtualFile>();
	private final Set<GitFile> changes = new LinkedHashSet<GitFile>();

	public GitChangeProvider( Project project, GitVcsSettings settings )
	{
		this.project = project;
		this.settings = settings;
	}

	public void getChanges( VcsDirtyScope dirtyScope, ChangelistBuilder builder, ProgressIndicator progress )
			throws VcsException
	{
		vcsRoots.clear();
		changes.clear();

		final Set<FilePath> dirtyDirectories = dirtyScope.getRecursivelyDirtyDirectories();
		for( FilePath filePath : dirtyDirectories )
		{
			processFile( filePath.getVirtualFile() );
		}

		final Set<FilePath> dirtyFiles = dirtyScope.getDirtyFiles();
		for( FilePath filePath : dirtyFiles )
		{
			processFile( filePath.getVirtualFile() );
		}

		for( GitFile gitFile : changes )
		{
			processChangedFile( builder, gitFile );
		}
	}

	private void processFile( VirtualFile file )
	{
		VirtualFile vcsRoot = GitUtil.getVcsRoot( project, file );

		if( vcsRoot == null || vcsRoots.contains( vcsRoot ) )
		{
			return;
		}

		com.assembla.git.commands.GitCommand command = new com.assembla.git.commands.GitCommand( project, settings, vcsRoot );
		try
		{
			//grab all changes per vcs root in one hg st command
			changes.addAll( command.status( vcsRoot.getPath(), false ) );
			vcsRoots.add( vcsRoot );
		}
		catch( VcsException e )
		{
			LOG.error( "Error reading file status", e );
		}
	}

	private void processChangedFile( ChangelistBuilder builder, GitFile file )
	{
		switch( file.getStatus() )
		{
			case ADDED:
			{
				builder.processChange(
						new Change( null, CurrentContentRevision.create( VcsUtil.getFilePath( file.getPath() ) ), FileStatus.ADDED ) );

				break;
			}
			case DELETED:
			{
				FilePath path = VcsUtil.getFilePath( file.getPath() );
				builder.processChange(
						new Change(
								new GitContentRevision( path, new GitRevisionNumber( GitRevisionNumber.TIP ), project ),
								null, FileStatus.DELETED ) );
				break;
			}
			case MODIFIED:
			{
				FilePath path = VcsUtil.getFilePath( file.getPath() );
				builder.processChange(
						new Change(
								new GitContentRevision( path, new GitRevisionNumber( GitRevisionNumber.TIP ), project ),
								CurrentContentRevision.create( path ), FileStatus.MODIFIED ) );
				break;
			}
			case UNMODIFIED:
			{
				FilePath path = VcsUtil.getFilePath( file.getPath() );
				VirtualFile virtualFile = VcsUtil.getVirtualFile( file.getPath() );
				if( virtualFile != null )
				{
					final boolean modified = FileDocumentManager.getInstance().isFileModified( virtualFile );
					if( modified )
					{
						builder.processChange(
								new Change(
										new GitContentRevision( path, new GitRevisionNumber( GitRevisionNumber.TIP ), project ),
										CurrentContentRevision.create( path ), FileStatus.MODIFIED )
						);
					}
				}
				break;
			}
			case UNVERSIONED:
			{
				VirtualFile virtualFile = VcsUtil.getVirtualFile( file.getPath() );
				builder.processUnversionedFile( virtualFile );
				break;
			}
			case IGNORED:
			{
				VirtualFile virtualFile = VcsUtil.getVirtualFile( file.getPath() );
				builder.processIgnoredFile( virtualFile );
				break;

			}
			default:
			{
				throw new IllegalArgumentException( "Unknown status: " + file.getStatus() );
			}
		}
	}

	public boolean isModifiedDocumentTrackingRequired()
	{
		return true;
	}
}
