package com.assembla.git;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.*;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.TreeItem;
import com.assembla.git.commands.GitCommand;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.ArrayList;

public class GitHistoryProvider implements VcsHistoryProvider
{
	private final Project project;
	private final GitVcsSettings settings;

	public GitHistoryProvider( Project project, GitVcsSettings settings )
	{
		this.project = project;
		this.settings = settings;
	}

	public ColumnInfo[] getRevisionColumns()
	{
		return new ColumnInfo[0];  //To change body of implemented methods use File | Settings | File Templates.
	}

	public AnAction[] getAdditionalActions( FileHistoryPanel panel )
	{
		return new AnAction[0];  //To change body of implemented methods use File | Settings | File Templates.
	}

	public boolean isDateOmittable()
	{
		return false;
	}

	@Nullable
	@NonNls
	public String getHelpId()
	{
		return null;
	}

	/**
	 * Provides the list of revisions for the specified file. This basically devolves to calling hg log for the file, parsing the output
	 * and stuffing it into the requisite structure for Idea.
	 *
	 * @param filePath The path to the file.
	 * @return A VcsSessionHistory that contains all the revisions of this file.
	 * @throws VcsException If it breaks.
	 */
	@Nullable
	public VcsHistorySession createSessionFor( FilePath filePath ) throws VcsException
	{
		GitCommand command = new GitCommand( project, settings, GitUtil.getVcsRoot(project, filePath) );
		List<VcsFileRevision> revisions = command.log( filePath );

		return new VcsHistorySession( revisions )
		{
			@Nullable
			protected VcsRevisionNumber calcCurrentRevisionNumber()
			{
				//todo
				return new GitRevisionNumber(GitRevisionNumber.TIP);
			}
		};
	}

	@Nullable
	public HistoryAsTreeProvider getTreeHistoryProvider()
	{
        return new HistoryAsTreeProvider() {
            // FIXMEignore branches for now...
            public List<TreeItem<VcsFileRevision>> createTreeOn(List<VcsFileRevision> vcsFileRevisions) {
                List<TreeItem<VcsFileRevision>> tree = new ArrayList<TreeItem<VcsFileRevision>>();
                for (VcsFileRevision revision : vcsFileRevisions) {
                    tree.add(new TreeItem<VcsFileRevision>(revision));

                }
                return tree;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
	}
}
