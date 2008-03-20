package com.assembla.git;

import com.intellij.openapi.command.CommandEvent;
import com.intellij.openapi.command.CommandListener;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsShowConfirmationOption;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.LocalFileOperationsHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.RefreshQueue;
import com.intellij.openapi.vfs.newvfs.RefreshSession;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GitFileSystemListener implements LocalFileOperationsHandler, CommandListener
{
	private static final Logger LOG = Logger.getInstance( com.assembla.git.GitFileSystemListener.class.getName() );

	private GitVcs host;
	private Project project;

	private final List<VirtualFile> filesToRefresh = new LinkedList<VirtualFile>();

	public GitFileSystemListener( @NotNull GitVcs host, @NotNull Project project )
	{
		this.host = host;
		this.project = project;
	}

	public boolean delete( VirtualFile file ) throws IOException
	{
		@NonNls final String TITLE = "Delete file(s)";
		@NonNls final String MESSAGE = "Do you want to schedule the following file for deletion from Git?\n{0}";

		//  In the case of multi-vcs project configurations, we need to skip all
		//  notifications on non-owned files
		if( !VcsUtil.isFileForVcs( file, project, host ) )
			return false;

		try
		{
			if( file.isDirectory() || getFileStatus( file, true ) == GitFile.Status.UNVERSIONED )
				return false;
		}
		catch( VcsException e )
		{
			e.printStackTrace();
		}

		//  Take into account only processable files.
		if( isFileProcessable( file ) )
		{
			VcsShowConfirmationOption option = host.getDeleteConfirmation();

			//  In the case when we need to perform "Delete" vcs action right upon
			//  the file's creation, put the file into the host's cache until it
			//  will be analyzed by the ChangeProvider.
			if( option.getValue() == VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY )
			{
				deleteFile( file );
			}
			else if( option.getValue() == VcsShowConfirmationOption.Value.SHOW_CONFIRMATION )
			{
				List<VirtualFile> files = new ArrayList<VirtualFile>();
				files.add( file );

				AbstractVcsHelper helper = AbstractVcsHelper.getInstance( project );
				Collection<VirtualFile> filesToProcess =
						helper.selectFilesToProcess( files, TITLE, null, TITLE, MESSAGE, option );

				if( filesToProcess != null )
				{
					return deleteFile( file );
				}
				else
				{
					return deleteFile( file );
				}
			}
			else
			{
				return deleteFile( file );
			}
			return true;
		}
		return false;
	}

	/**
	 * Issues the git command to remove the file.
	 *
	 * @param file The file to remove.
	 */
	private boolean deleteFile( VirtualFile file )
	{
		com.assembla.git.commands.GitCommand command = new com.assembla.git.commands.GitCommand( project, host.getSettings(), GitUtil.getVcsRoot( project, file ) );
		VirtualFile[] files = new VirtualFile[1];
		files[0] = file;
		try
		{

			Set<GitFile> mFiles = command.status( file.getPath(), true );
			for( GitFile f : mFiles )
			{
				if( f.getStatus() == GitFile.Status.ADDED )
				{
					// We revert and delete from the file system.
					command = new com.assembla.git.commands.GitCommand( project, host.getSettings(), GitUtil.getVcsRoot( project, file ) );
					files[0] = VcsUtil.getVirtualFile( f.getPath() );
					command.revert( files );
					return false;
				}
				else if( f.getStatus() == GitFile.Status.MODIFIED )
				{
					// We revert and delete from the file system.
					command = new com.assembla.git.commands.GitCommand( project, host.getSettings(), GitUtil.getVcsRoot( project, file ) );
					files[0] = VcsUtil.getVirtualFile( f.getPath() );
					command.revert( files );
					command = new com.assembla.git.commands.GitCommand( project, host.getSettings(), GitUtil.getVcsRoot( project, file ) );
					command.delete( files );
					return true;
				}
				else
				{
					// We get Git to do it.
					command = new com.assembla.git.commands.GitCommand( project, host.getSettings(), GitUtil.getVcsRoot( project, file ) );
					files[0] = VcsUtil.getVirtualFile( f.getPath() );
					command.delete( files );
					return true;
				}
			}
			return false;
		}
		catch( VcsException e )
		{
			LOG.error( e );
			e.printStackTrace();
			return false;
		}
	}

	private GitFile.Status getFileStatus( VirtualFile file, boolean forceStatus ) throws VcsException
	{
		com.assembla.git.commands.GitCommand command = new com.assembla.git.commands.GitCommand( project, host.getSettings(), GitUtil.getVcsRoot( project, file ) );
		Set<GitFile> files = command.status( file.getPath(), forceStatus );
		assert files.size() == 1 : "Returned more than one status";

		GitFile mFile = files.iterator().next();
		return mFile.getStatus();
	}


	public boolean move( VirtualFile file, VirtualFile toDir ) throws IOException
	{
		com.assembla.git.commands.GitCommand moveCommand =
				new com.assembla.git.commands.GitCommand( project, host.getSettings(), GitUtil.getVcsRoot( project, file ) );
		try
		{
			File destinationFile = new File( new File( toDir.getPath() ).getAbsoluteFile(), file.getName() );
			moveCommand.move( file, toDir );
			destinationFile.setLastModified( file.getTimeStamp() );
			filesToRefresh.add( file.getParent() );
			filesToRefresh.add( toDir );
			return true;
		}
		catch( VcsException e )
		{
			LOG.error( e );
		}
		return false;
	}

	@Nullable
	public File copy( VirtualFile file, VirtualFile toDir, String copyName ) throws IOException
	{
		com.assembla.git.commands.GitCommand copyCommand =
				new com.assembla.git.commands.GitCommand( project, host.getSettings(), GitUtil.getVcsRoot( project, file ) );
		try
		{
			File destinationFile = new File( new File( toDir.getPath() ).getAbsoluteFile(), copyName );
			copyCommand.copy( file, toDir, copyName );
			destinationFile.setLastModified( file.getTimeStamp() );
			filesToRefresh.add( file.getParent() );
			filesToRefresh.add( toDir );
			return null;
		}
		catch( VcsException e )
		{
			LOG.error( e );
		}
		return null;
	}

	public boolean rename( VirtualFile file, String newName ) throws IOException
	{
		com.assembla.git.commands.GitCommand command =
				new com.assembla.git.commands.GitCommand( project, host.getSettings(), GitUtil.getVcsRoot( project, file ) );

		try
		{
			File oldFile = new File( file.getPath() );
			File newFile = new File( oldFile.getParent(), newName );
			long timeStamp = file.getTimeStamp();
			command.rename( file, newName );
			newFile.setLastModified( timeStamp );
			filesToRefresh.add( file.getParent() );
			return true;
		}
		catch( VcsException e )
		{
			LOG.error( e );
		}
		return false;
	}

	public boolean createFile( VirtualFile dir, String name ) throws IOException
	{
		return false;
	}

	public boolean createDirectory( VirtualFile dir, String name ) throws IOException
	{
		return false;
	}

	public void commandStarted( CommandEvent event )
	{
	}

	public void beforeCommandFinished( CommandEvent event )
	{
	}

	public void commandFinished( CommandEvent event )
	{
		if( filesToRefresh.isEmpty() )
		{
			return;
		}

		final Project project = event.getProject();
		if( project == null )
		{
			return;
		}

		final List<VirtualFile> refreshBatch = new ArrayList<VirtualFile>( filesToRefresh );
		filesToRefresh.clear();

		final RefreshSession session = RefreshQueue.getInstance().createSession( true, true, new Runnable()
		{
			public void run()
			{
				if( project.isDisposed() ) return;
				for( VirtualFile f : refreshBatch )
				{
					if( !f.isValid() ) continue;
					if( f.isDirectory() )
					{
						VcsDirtyScopeManager.getInstance( project ).dirDirtyRecursively( f );
					}
					else
					{
						VcsDirtyScopeManager.getInstance( project ).fileDirty( f );
					}
				}
			}
		} );
		session.addAllFiles( refreshBatch );
		session.launch();
	}

	public void undoTransparentActionStarted()
	{
	}

	public void undoTransparentActionFinished()
	{
	}

	/**
	 * File is not processable if it is outside the vcs scope or it is in the
	 * list of excluded project files.
	 *
	 * @param file The file to check.
	 * @return Returns true of the file can be added.
	 */
	private boolean isFileProcessable( VirtualFile file )
	{
		return VcsUtil.isPathUnderProject( project, file ) 
				&& !FileTypeManager.getInstance().isFileIgnored( file.getName() )
				&& !file.isDirectory() && !file.getPath().startsWith( ".hg/" );
	}

}
