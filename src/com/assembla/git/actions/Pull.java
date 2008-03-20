package com.assembla.git.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Pull extends BasicAction
{
	protected void perform( @NotNull Project project, com.assembla.git.GitVcs vcs, @NotNull List<VcsException> exceptions,
	                        @NotNull VirtualFile[] affectedFiles ) throws VcsException
	{
		saveAll();

		final String respository = Messages.showInputDialog( project, "Specify source respository", "Pull", Messages.getQuestionIcon() );
		if( respository == null )
			return;

        final VirtualFile[] roots = ProjectLevelVcsManager.getInstance(project).getRootsUnderVcs(vcs);
        //todo
        assert roots.length == 1 : "more than 1 root push is not supported";

        for (VirtualFile root : roots) {
            com.assembla.git.commands.GitCommand command = new com.assembla.git.commands.GitCommand( project, vcs.getSettings(), com.assembla.git.GitUtil.getVcsRoot(project, root) );
            command.pull( respository, true );

            /*
            /* TODO: Do a merge after the pull. Currently, this will pop up the configured merge tool for conflicts.
             * Integrating this into Idea may be challenging as hg merge wants an executable...
             */
            command.merge();
        }

	}

	@NotNull
	protected String getActionName( @NotNull AbstractVcs abstractvcs )
	{
		return "Pull";
	}

	protected boolean isEnabled( @NotNull Project project, @NotNull com.assembla.git.GitVcs mksvcs, @NotNull VirtualFile... vFiles )
	{
		return true;
	}
}
