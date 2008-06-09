package com.assembla.git;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;

public class GitRevisionNumber implements VcsRevisionNumber
{
	public static final String TIP = "HEAD";
	
    private final String version;

    public GitRevisionNumber(String version) {
        this.version = version;
    }

    public String asString()
	{
		return version;
	}

    // TODO ordering on hash doesn't make sense
    public int compareTo( VcsRevisionNumber vcsRevisionNumber )
	{
		GitRevisionNumber rev = (GitRevisionNumber) vcsRevisionNumber;

		if( getVersion().equals(rev.getVersion()))
			return 0;
		else
			return 1;
	}

	public String getVersion()
	{
		return version;
	}
}
