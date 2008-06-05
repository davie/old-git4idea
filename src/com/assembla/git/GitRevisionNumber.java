package com.assembla.git;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;

public class GitRevisionNumber implements VcsRevisionNumber
{
	public static final String TIP = "-1";
	
    private final String version;

    public GitRevisionNumber(String version) {
        this.version = version;
    }

    public String asString()
	{
		if( TIP.equals(version) )
			return "tip";
		return String.valueOf( version );
	}

    // TODO ordering on hash doesn't make sense
    public int compareTo( VcsRevisionNumber vcsRevisionNumber )
	{
		GitRevisionNumber rev = (GitRevisionNumber) vcsRevisionNumber;

		if( getNumber().equals(rev.getNumber()))
			return 0;
		else
			return 1;
	}

	public String getNumber()
	{
		return version;
	}
}
