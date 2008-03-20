package com.assembla.git.actions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class Revert extends BasicAction
{
	/**
	 * Revert the selected file.
	 */
	public void perform( @NotNull Project project, com.assembla.git.GitVcs vcs, @NotNull List<VcsException> exceptions,
	                     @NotNull VirtualFile[] affectedFiles ) throws VcsException
	{
		ApplicationManager.getApplication().runWriteAction(
				new Runnable()
				{
					public void run()
					{
						FileDocumentManager.getInstance().saveAllDocuments();
					}
				} );

        final Map<VirtualFile,List<VirtualFile>> roots = com.assembla.git.GitUtil.sortFilesByVcsRoot(project, affectedFiles);

        for (VirtualFile root : roots.keySet()) {
            com.assembla.git.commands.GitCommand command = new com.assembla.git.commands.GitCommand( project, com.assembla.git.GitVcsSettings.getInstance( project ), root );
            command.revert( roots.get(root) );
        }

        VcsDirtyScopeManager mgr = VcsDirtyScopeManager.getInstance( project );
        for( VirtualFile file : affectedFiles )
        {
            mgr.fileDirty( file );
            file.refresh( true, true );
        }
	}

	@NotNull
	protected String getActionName( @NotNull AbstractVcs abstractvcs )
	{
		return "Revert";
	}

	protected boolean isEnabled( @NotNull Project project, @NotNull com.assembla.git.GitVcs vcs, @NotNull VirtualFile... vFiles )
	{
		for( VirtualFile file : vFiles )
		{
			if( FileStatusManager.getInstance( project ).getStatus( file ) == FileStatus.UNKNOWN )
				return false;
			if( FileStatusManager.getInstance( project ).getStatus( file ) == FileStatus.NOT_CHANGED )
				return false;
		}

		return true;
	}
}
