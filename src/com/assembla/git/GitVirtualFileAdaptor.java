package com.assembla.git;

import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsShowConfirmationOption;
import com.intellij.openapi.vfs.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GitVirtualFileAdaptor extends VirtualFileAdapter
{
	private Project project;
	private GitVcs host;
	private static final String TITLE = "Add file(s)";
	private static final String MESSAGE = "Add files to Git?\n{0}";

	public GitVirtualFileAdaptor( @NotNull GitVcs host, @NotNull Project project )
	{
		this.host = host;
		this.project = project;
	}

	public void propertyChanged( VirtualFilePropertyEvent event )
	{
		super.propertyChanged( event );    //To change body of overridden methods use File | Settings | File Templates.
	}

	public void contentsChanged( VirtualFileEvent event )
	{
		super.contentsChanged( event );    //To change body of overridden methods use File | Settings | File Templates.
	}

	/**
	 * Called when a new file is added.
	 *
	 * @param event The event from Idea.
	 */
	public void fileCreated( VirtualFileEvent event )
	{
		if( event.isFromRefresh() )
			return;

		final VirtualFile file = event.getFile();

		if( isFileProcessable( file ) )
		{
			VcsShowConfirmationOption option = host.getAddConfirmation();
			if( option.getValue() == VcsShowConfirmationOption.Value.SHOW_CONFIRMATION )
			{
				List<VirtualFile> files = new ArrayList<VirtualFile>();
				files.add( file );

				AbstractVcsHelper helper = AbstractVcsHelper.getInstance( project );
				Collection<VirtualFile> filesToAdd = helper.selectFilesToProcess( files, TITLE, null, TITLE, MESSAGE, option );

				if( filesToAdd != null )
				{
					com.assembla.git.commands.GitCommand command = new com.assembla.git.commands.GitCommand( project, host.getSettings(), GitUtil.getVcsRoot( project, file ) );
					try
					{
						command.add( filesToAdd.toArray() );
					}
					catch( VcsException e )
					{
						List<VcsException> es = new ArrayList<VcsException>();
						es.add( e );
						GitVcs.getInstance( project ).showErrors( es, "Changes" );
					}
				}
			}
		}

	}

	public void fileDeleted( VirtualFileEvent event )
	{
		super.fileDeleted( event );    //To change body of overridden methods use File | Settings | File Templates.
	}

	public void fileMoved( VirtualFileMoveEvent event )
	{
		super.fileMoved( event );    //To change body of overridden methods use File | Settings | File Templates.
	}

	public void fileCopied( VirtualFileCopyEvent event )
	{
		super.fileCopied( event );    //To change body of overridden methods use File | Settings | File Templates.
	}

	public void beforePropertyChange( VirtualFilePropertyEvent event )
	{
		super.beforePropertyChange( event );    //To change body of overridden methods use File | Settings | File Templates.
	}

	public void beforeContentsChange( VirtualFileEvent event )
	{
		super.beforeContentsChange( event );
	}

	public void beforeFileDeletion( VirtualFileEvent event )
	{
	}

	public void deleteFile( VirtualFile file )
	{
//		GitCommand command = new GitCommand( project, host.getSettings(), GitUtil.getVcsRoot( project, file ) );
//		VirtualFile[] files = new VirtualFile[1];
//		files[0] = file;
//		try
//		{
//			command.delete( files );
//		}
//		catch( VcsException e )
//		{
//			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//		}

	}

	public void beforeFileMovement( VirtualFileMoveEvent event )
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
		VirtualFile base = project.getBaseDir();
		assert base != null;
		return file.getPath().startsWith( base.getPath() ) && !FileTypeManager.getInstance().isFileIgnored( file.getName() )
				&& !file.isDirectory() && !file.getPath().startsWith( ".hg/" );
	}
}
