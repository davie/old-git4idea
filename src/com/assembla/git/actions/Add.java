package com.assembla.git.actions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.VirtualFile;
import static com.assembla.git.GitUtil.*;
import com.assembla.git.commands.GitCommand;
import com.assembla.git.GitVcsSettings;
import com.assembla.git.GitVcs;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class Add extends BasicAction
{
	public void perform( @NotNull Project project, GitVcs mksVcs, @NotNull List<VcsException> exceptions,
	                     @NotNull VirtualFile[] affectedFiles ) throws VcsException
	{
        addFiles(project, affectedFiles);
	}

    public static void addFiles(Project project, VirtualFile[] affectedFiles) throws VcsException {
        ApplicationManager.getApplication().runWriteAction(
				new Runnable()
                {
                    public void run()
                    {
                        FileDocumentManager.getInstance().saveAllDocuments();
                    }
                } );

        if( !ProjectLevelVcsManager.getInstance( project ).checkAllFilesAreUnder( GitVcs.getInstance( project ), affectedFiles ) )
            return;

        final Map<VirtualFile,List<VirtualFile>> roots = sortFilesByVcsRoot(project, affectedFiles);

        for (VirtualFile root : roots.keySet()) {
            GitCommand command = new GitCommand( project, GitVcsSettings.getInstance( project ), root );
            command.add( roots.get(root) );
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

	protected boolean isEnabled( @NotNull Project project, @NotNull GitVcs vcs, @NotNull VirtualFile... vFiles )
	{
		for( VirtualFile file : vFiles )
		{
			if( FileStatusManager.getInstance( project ).getStatus( file ) != FileStatus.UNKNOWN )
				return false;
		}

		return true;
	}
}
