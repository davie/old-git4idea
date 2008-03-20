package com.assembla.git.actions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class Delete extends BasicAction
{
	public void perform( @NotNull Project project, com.assembla.git.GitVcs mksVcs, @NotNull List<VcsException> exceptions,
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

		if( !ProjectLevelVcsManager.getInstance( project ).checkAllFilesAreUnder( com.assembla.git.GitVcs.getInstance( project ), affectedFiles ) )
			return;

        final Map<VirtualFile,List<VirtualFile>> roots = com.assembla.git.GitUtil.sortFilesByVcsRoot(project, affectedFiles);

        for (VirtualFile root : roots.keySet()) {
            com.assembla.git.commands.GitCommand command = new com.assembla.git.commands.GitCommand( project, com.assembla.git.GitVcsSettings.getInstance( project ), root );
            command.delete( roots.get(root) );
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
		return "Add";
	}

	protected boolean isEnabled( @NotNull Project project, @NotNull com.assembla.git.GitVcs vcs, @NotNull VirtualFile... vFiles )
	{
		for( VirtualFile file : vFiles )
		{
			if( FileStatusManager.getInstance( project ).getStatus( file ) == FileStatus.UNKNOWN )
				return false;
		}

		return true;
	}
}
