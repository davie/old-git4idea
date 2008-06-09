package com.assembla.git;

public class GitFile
{
	private final String path;
	private final Status status;

	public GitFile( String path, Status status )
	{
		this.path = path;
		this.status = status;
	}

	public String getPath()
	{
		return path;
	}

    public Status getStatus()
	{
		return status;
	}

    public enum Status {
        ADDED,
        MODIFIED,
        UNVERSIONED,
        UNMODIFIED,
        DELETED,
		IGNORED
    }
}
