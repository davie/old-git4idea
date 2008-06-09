package com.assembla.git.commands;

import com.assembla.git.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

@SuppressWarnings( { "ResultOfMethodCallIgnored" } )
public class GitCommand
{
	private static final Logger LOG = Logger.getInstance( "com.assembla.git.GitVcs" );

	public static final String ADD_CMD = "add";
	public static final String REVERT_CMD = "revert";
    private static final String SHOW_CMD = "show";
	private static final String DELETE_CMD = "rm";

	private final Project project;
    private final VirtualFile vcsRoot;
    private final GitLogParser gitLogParser = new GitLogParser();
    private final CommandExecutor commandExecutor;

    public GitCommand( @NotNull final Project project, @NotNull GitVcsSettings settings, @NotNull VirtualFile vcsRoot )
	{
		this.vcsRoot = vcsRoot;
		this.project = project;
        this.commandExecutor = new CommandExecutor(settings, project, vcsRoot);
    }


	public void add( VirtualFile[] files ) throws VcsException
	{
        String[] fixedFileNames = new String[files.length];
		int count = 0;
		for( VirtualFile file : files )
		{
			if( file.getPath().startsWith(basePath()) )
			{
				fixedFileNames[count] = getRelativeFilePath( file, vcsRoot);
				count++;
			}
			else
				GitVcs.getInstance( project ).showMessages( "Not in scope: " + file.getPath() );
		}

		commandExecutor.execute( ADD_CMD, (String[]) null, fixedFileNames );
	}

	private String getRelativeFilePath( VirtualFile file, @NotNull final VirtualFile baseDir )
	{
		return getRelativeFilePath( file.getPath(), baseDir );
	}

	private String getRelativeFilePath( String file, @NotNull final VirtualFile baseDir )
	{
        final String basePath = baseDir.getPath();
		if( !file.startsWith( basePath ) )
			return file;
		else if( file.equals( basePath ) )
			return ".";
		return file.substring( baseDir.getPath().length() + 1 );
	}


	public void add( Object[] files ) throws VcsException
	{
		VirtualFile[] arr = new VirtualFile[files.length];
		for( int i = 0; i < files.length; i++ )
			arr[i] = (VirtualFile) files[i];
		add( arr );
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
		return status( null, false );
	}


	public Set<GitFile> status( String path, boolean includeAll ) throws VcsException
	{

		List<String> args = new ArrayList<String>();
        // ignore the cache for now
        //        args.add("-c");
        args.add("-d");
        args.add("-m");
        args.add("-o");
        args.add("-t");
        if( includeAll )
			args.add( "-A" );
		if( path != null )
			args.add( getRelativeFilePath( path, vcsRoot ) );

        // -c gives cached, we might not need this.
        // to get a more parsable form of output (possibly -z too)
        String output = convertStreamToString( commandExecutor.execute( "ls-files", args ) );
        return new GitStatusParser(basePath()).parse(output);
	}


    /**
	 * Loads a file from Git.
	 *
	 * @param path	 The path to the file.
	 * @param revision The revision to load. If the revision is -1, then the tip will be loaded.
	 * @return The contents of the revision as a String.
	 * @throws VcsException If the load of the file fails.
	 */
	public byte[] cat( String path, String revision ) throws VcsException
	{
		if( path == null || path.equals( "" ) )
			throw new VcsException( "Illegal argument to show" );

		String vcsPath = getRelativeFilePath( path, vcsRoot );
        String options = revision + ":" + vcsPath;

		InputStream in = commandExecutor.execute(SHOW_CMD, options);

		try
		{
			byte[] content = new byte[in.available()];
			in.read( content, 0, in.available() );
			in.close();
			return content;
		}
		catch( IOException e )
		{
			throw new VcsException( e );
		}
	}

	/**
	 * Reverts the list of files we are passed.
	 *
	 * @param files The array of files to revert.
	 * @throws VcsException Id it breaks.
	 */
	public void revert( VirtualFile[] files ) throws VcsException
	{
		String baseDirStr = basePath();
		String[] fixedFileNames = new String[files.length];
		int count = 0;
		for( VirtualFile file : files )
		{
			if( file.getPath().substring( 0, baseDirStr.length() ).equals( baseDirStr ) )
			{
				fixedFileNames[count] = getRelativeFilePath( file, vcsRoot );
				count++;
			}
			else
				GitVcs.getInstance( project ).showMessages( "Not in scope: " + file.getPath() );
		}
		commandExecutor.execute( REVERT_CMD, "--no-backup", fixedFileNames );
	}

    public static String convertStreamToString( InputStream in ) throws VcsException
	{
		BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );
		StringWriter result = new StringWriter();

		int count;
		char[] buf = new char[4096];
		try
		{
			while( (count = reader.read( buf, 0, 4096 )) != -1 )
				result.write( buf, 0, count );
			reader.close();
		}
		catch( IOException e )
		{
			throw new VcsException( e );
		}
		return result.toString().replaceAll( "\\r", "" );
	}

	/**
	 * Returns the base path of the project.
	 *
	 * @return The base path of the project.
	 */
	private String basePath()
	{
		return vcsRoot.getPath();
	}

	public void commit( Set<String> paths, String message ) throws VcsException
	{
		String[] options = new String[2];
		options[0] = "-m";
		options[1] = message;

		String[] args = new String[paths.size()];
		int i = 0;
		for( String path : paths )
			args[i++] = getRelativeFilePath( path, vcsRoot );

		commandExecutor.execute( "commit", options, args );
	}

	public void delete( VirtualFile[] files ) throws VcsException
	{
		String baseDirStr = basePath();
		String[] fixedFileNames = new String[files.length];
		int count = 0;
		for( VirtualFile file : files )
		{
			if( file.getPath().substring( 0, baseDirStr.length() ).equals( baseDirStr ) )
			{
				fixedFileNames[count] = getRelativeFilePath( file, vcsRoot );
				count++;
			}
			else
				GitVcs.getInstance( project ).showMessages( "Not in scope: " + file.getPath() );
		}

		commandExecutor.execute( DELETE_CMD, (String[]) null, fixedFileNames );
	}

	/**
	 * Builds the revision history for the specifid file.
	 *
	 * @param filePath The path to the file.
	 * @return The list.
	 * @throws com.intellij.openapi.vcs.VcsException
	 *          If it fails...
	 */
	public List<VcsFileRevision> log( FilePath filePath ) throws VcsException
	{
		String[] options = new String[]
				{
						"--pretty=format:%H|%an|%ci|%s"
				};


        String[] args = new String[]
				{
						getRelativeFilePath( filePath.getPath(), vcsRoot )
				};

		String result = convertStreamToString( commandExecutor.execute( "log", options, args ) );
		GitVcs.getInstance( project ).showMessages( result );


        return gitLogParser.getRevisionsFrom(filePath, result, project);
	}

    public String version() throws VcsException
	{
		return convertStreamToString( commandExecutor.execute( "version" ) );
	}

	public String tag( String tagName ) throws VcsException
	{
		return convertStreamToString( commandExecutor.execute( "tag", tagName ) );
	}

	public void pull( String respository, boolean update ) throws VcsException
	{
		String options = null;
		if( update )
			options = "-u";

		String result = convertStreamToString( commandExecutor.execute( "pull", options, respository ) );
		GitVcs.getInstance( project ).showMessages( result );
	}

	public void merge() throws VcsException
	{
		commandExecutor.execute( "merge" );
	}

	public void push( String repository ) throws VcsException
	{
		String result = convertStreamToString( commandExecutor.execute( "push", repository ) );
		GitVcs.getInstance( project ).showMessages( result );
	}

	public void delete( List<VirtualFile> files ) throws VcsException
	{
		delete( files.toArray( new VirtualFile[files.size()] ) );
	}

	public void revert( List<VirtualFile> files ) throws VcsException
	{
		revert( files.toArray( new VirtualFile[files.size()] ) );
	}

	public void add( List<VirtualFile> files ) throws VcsException
	{
		add( files.toArray( new VirtualFile[files.size()] ) );
	}

    /**
	 * Moves a file to a new directory.
	 *
	 * @param file	  The file to move.
	 * @param newParent The parent directory to move the file to.
	 * @throws com.intellij.openapi.vcs.VcsException
	 *          IF it breaks.
	 */
	public void move( VirtualFile file, VirtualFile newParent ) throws VcsException
	{
		String[] args = new String[2];

		args[0] = getRelativeFilePath( file, vcsRoot );
		args[1] = getRelativeFilePath( newParent.getPath() + "/" + file.getName(), vcsRoot );
		commandExecutor.execute( "rename", (String[]) null, args );
	}

    /**
	 * Moves a file to a new directory.
	 *
	 * @param file	The file to move.
	 * @param newName The name of the new file.
	 * @throws com.intellij.openapi.vcs.VcsException
	 *          IF it breaks.
	 */
	public void rename( VirtualFile file, String newName ) throws VcsException
	{
		String[] args = new String[2];

		args[0] = getRelativeFilePath( file, vcsRoot );
		args[1] = getRelativeFilePath( file.getParent().getPath() + "/" + newName, vcsRoot );
		commandExecutor.execute( "rename", (String[]) null, args );
	}

    /**
	 * Copies a file
	 *
	 * @param file	The file to copy.
	 * @param toDir	Dir to copy file.
	 * @param copyName The name of the new file.
	 * @throws com.intellij.openapi.vcs.VcsException
	 *          IF it breaks.
	 */
	public void copy( VirtualFile file, VirtualFile toDir, String copyName ) throws VcsException
	{
		String[] args = new String[2];

		args[0] = getRelativeFilePath( file, vcsRoot );
		args[1] = getRelativeFilePath( toDir.getPath() + "/" + copyName, vcsRoot );
		commandExecutor.execute( "copy", (String[]) null, args );
	}

    /**
     * Clones the repository to the specified path.
     *
     * @param src	The src repository. May be a URL or a path.
     * @param target The target directory.
     * @throws com.intellij.openapi.vcs.VcsException
     *          If an error occurs.
     */
    public void cloneRepository( String src, String target ) throws VcsException
    {
        String[] args = new String[2];
        args[0] = src;
        args[1] = target;

        commandExecutor.execute( "clone", (String) null, args );
    }

}
