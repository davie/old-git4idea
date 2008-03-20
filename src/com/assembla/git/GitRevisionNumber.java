package com.assembla.git;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;

public class GitRevisionNumber implements VcsRevisionNumber
{
	public static final int TIP = -1;
	
	private int number;

	public GitRevisionNumber( int version )
	{
		this.number = version;
	}

	public String asString()
	{
		if( number == TIP )
			return "tip";
		return String.valueOf( number );
	}

	public int compareTo( VcsRevisionNumber vcsRevisionNumber )
	{
		com.assembla.git.GitRevisionNumber rev = (com.assembla.git.GitRevisionNumber) vcsRevisionNumber;

		if( getNumber() < rev.getNumber() )
			return -1;
		else if( getNumber() == rev.getNumber() )
			return 0;
		else
			return 1;
	}

	public int getNumber()
	{
		return number;
	}
}
