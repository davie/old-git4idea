package com.assembla.git;

public class GitFile
{
	private String path;
	private Status status;

	public GitFile( String path, Status status )
	{
		this.path = path;
		this.status = status;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath( String path )
	{
		this.path = path;
	}

	public Status getStatus()
	{
		return status;
	}

	public void setStatus( Status status )
	{
		this.status = status;
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
