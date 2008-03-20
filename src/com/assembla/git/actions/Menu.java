package com.assembla.git.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.actions.StandardVcsGroup;

public class Menu extends StandardVcsGroup
{
	public AbstractVcs getVcs( Project project )
	{
		return com.assembla.git.GitVcs.getInstance( project );
	}

	@Override
	public String getVcsName( final Project project )
	{
		return "Git";
	}
}
