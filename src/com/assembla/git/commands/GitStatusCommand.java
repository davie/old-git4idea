package com.assembla.git.commands;

import com.assembla.git.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.VcsException;

import java.io.*;
import java.util.*;

@SuppressWarnings( { "ResultOfMethodCallIgnored" } )
public class GitStatusCommand
{
	private static final Logger LOG = Logger.getInstance( "com.assembla.git.GitVcs" );

	public static final String ADD_CMD = "add";

    private final CommandExecutor commandExecutor;
    private File vcsRootDirectory;
    private IErrorHandler errorHandler;

    public GitStatusCommand(IErrorHandler errorHandler, File vcsRootDirectory, String gitExecutable)
	{
        this.vcsRootDirectory = vcsRootDirectory;
        this.errorHandler = errorHandler;
        this.commandExecutor = new CommandExecutor(gitExecutable, errorHandler, vcsRootDirectory);
    }


    private String getRelativeFilePath( String file, String basePath )
	{
		if( !file.startsWith( basePath ) )
			return file;
		else if( file.equals( basePath ) )
			return ".";
		return file.substring( basePath.length() + 1 );
	}



	/**
	 * Returns a list of files that contain the status of that file.
	 *
	 * @return The set of files.
	 * @throws com.intellij.openapi.vcs.VcsException
	 *          If it fails.
	 */
	public Set<GitFile> status() throws VcsException
	{
		return status( null);
	}


	public Set<GitFile> status(String path) throws VcsException
	{

		List<String> args = new ArrayList<String>();
        // ignore the cache for now
        //        args.add("-c");
        args.add("-d");
        args.add("-m");
        args.add("-o");

        args.add("-c");
        args.add("-t");
		if( path != null )
			args.add( getRelativeFilePath( path, basePath() ) );

        // -c gives cached, we might not need this.
        // to get a more parsable form of output (possibly -z too)
        String output = GitCommand.convertStreamToString( commandExecutor.execute( "ls-files", args ) );
        return new GitStatusParser(basePath()).parse(output);
	}


	/**
	 * Returns the base path of the project.
	 *
	 * @return The base path of the project.
	 */
	private String basePath()
	{
		return vcsRootDirectory.getAbsolutePath();
	}

    public void init() throws VcsException {
        commandExecutor.execute("init");
    }

	public void add( File... files ) throws VcsException
	{
        String[] fixedFileNames = new String[files.length];
		int count = 0;
		for( File file : files )
		{
			if( file.getPath().startsWith(basePath()) )
			{
				fixedFileNames[count] = getRelativeFilePath( file.getAbsolutePath(), basePath());
				count++;
			}
			else
				errorHandler.displayErrorMessage( "Not in scope: " + file.getPath() );
		}

		commandExecutor.execute( ADD_CMD, (String[]) null, fixedFileNames );
	}
}