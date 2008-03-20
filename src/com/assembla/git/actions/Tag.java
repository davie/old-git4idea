package com.assembla.git.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Tag extends BasicAction
{
	public void perform( @NotNull Project project, com.assembla.git.GitVcs vcs, @NotNull List<VcsException> exceptions,
	                     @NotNull VirtualFile[] affectedFiles ) throws VcsException
	{
		saveAll();

		if( !ProjectLevelVcsManager.getInstance( project ).checkAllFilesAreUnder( vcs, affectedFiles ) )
			return;


		final String tagName = Messages.showInputDialog( project, "Specify tag name", "Tag", Messages.getQuestionIcon() );
		if( tagName == null )
			return;

        //todo: support multiple roots?
        com.assembla.git.commands.GitCommand command = new com.assembla.git.commands.GitCommand(
                project,
                vcs.getSettings() ,
                com.assembla.git.GitUtil.getVcsRoot(project, affectedFiles[0]));

        final String output = command.tag( tagName );
		if( output.trim().length() != 0 )
		{
			Messages.showInfoMessage( project, output, "Result" );
		}
	}

	@NotNull
	protected String getActionName( @NotNull AbstractVcs abstractvcs )
	{
		return "Tag";
	}

	protected boolean isEnabled( @NotNull Project project, @NotNull com.assembla.git.GitVcs vcs, @NotNull VirtualFile... vFiles )
	{
		return true;
	}
}
