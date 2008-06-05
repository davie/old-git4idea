package com.assembla.git.commands;

import com.assembla.git.*;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.EnvironmentUtil;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

@SuppressWarnings( { "ResultOfMethodCallIgnored" } )
public class GitCommand
{
	private static final Logger LOG = Logger.getInstance( "com.assembla.git.GitVcs" );

	public static final String ADD_CMD = "add";
	public static final String REVERT_CMD = "revert";
    // FIXME cat doesn't exist in git
    private static final String CAT_CMD = "show";
	private static final String DELETE_CMD = "rm";

	private Project project;
	private final GitVcsSettings settings;
	private VirtualFile vcsRoot;
    private GitLogParser gitLogParser = new GitLogParser();

    public GitCommand( @NotNull final Project project, @NotNull GitVcsSettings settings, @NotNull VirtualFile vcsRoot )
	{
		this.vcsRoot = vcsRoot;
		this.project = project;
		this.settings = settings;
	}


	public void add( VirtualFile[] files ) throws VcsException
	{
		String baseDirStr = getBasePath();
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

		execute( ADD_CMD, (String[]) null, fixedFileNames );
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

        // DM TODO change the git command to:
        // git-ls-files -c -d -m -o  -t
        // -c gives cached, we might not need this.
        // to get a more parsable form of output (possibly -z too)
        String output = convertStreamToString( execute( "ls-files", args ) );
        return new GitStatusParser(getBasePath()).parse(output);
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

		InputStream in = execute( CAT_CMD, options);

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
		String baseDirStr = getBasePath();
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
		execute( REVERT_CMD, "--no-backup", fixedFileNames );
	}

	private InputStream execute( String cmd, String arg ) throws VcsException
	{
		return execute( cmd, null, arg );
	}

	private InputStream execute( String cmd, String oneOption, String[] args ) throws VcsException
	{
		String[] options = new String[1];
		options[0] = oneOption;

		return execute( cmd, options, args );
	}

	private InputStream execute( String cmd, String option, String arg ) throws VcsException
	{
		String[] options = null;
		if( option != null )
		{
			options = new String[1];
			options[0] = option;
		}
		String[] args = null;
		if( arg != null )
		{
			args = new String[1];
			args[0] = arg;
		}

		return execute( cmd, options, args );
	}

	private InputStream execute( String cmd, String[] options, String[] args ) throws VcsException
	{
		List<String> cmdLine = new ArrayList<String>();
		if( options != null )
		{
			cmdLine.addAll( Arrays.asList( options ) );
		}
		if( args != null )
		{
			cmdLine.addAll( Arrays.asList( args ) );
		}
		return execute( cmd, cmdLine );
	}

	private InputStream execute( String cmd ) throws VcsException
	{
		return execute( cmd, Collections.<String>emptyList() );
	}

	private InputStream execute( String cmd, List<String> cmdArgs ) throws VcsException
	{
		/*
		 * First, we build the proper command line. Then we execute it.
		 */

		PathManager.getPluginsPath();


		List<String> cmdLine = new ArrayList<String>();

		String[] execCmds = settings.GIT_EXECUTABLE.split( " " );
		for( String c : execCmds )
			cmdLine.add( c );

		cmdLine.add( cmd );
		cmdLine.addAll( cmdArgs );

		String cmdString = StringUtil.join( cmdLine, " " );
		GitVcs.getInstance( project ).showMessages( "CMD: " + cmdString );

		/*
		 * We now have the command line, so we execute it.
		 */
		File directory = VfsUtil.virtualToIoFile( vcsRoot );

		try
		{
			final Map<String, String> environment = EnvironmentUtil.getEnviromentProperties();

			ProcessBuilder builder = new ProcessBuilder( cmdLine );
			builder.directory( directory );

			Map<String, String> defaultEnv = builder.environment();
			// TODO: This may be completely redundant. Where does Idea get it's env. from?
			// This assumes that we replace the envionment
			for( String key : environment.keySet() )
				defaultEnv.put( key, environment.get( key ) );

			Process proc = builder.start();
			final InputStream in = proc.getInputStream();
			final ByteArrayOutputStream out = new ByteArrayOutputStream();

			/*
			 * Start a thread to read the input stream. This prevents the process from blocking in
			 * waitFor() when the output buffer fills up.
			 */
			new Thread(
					new Runnable()
					{
						public void run()
						{
							byte[] buf = new byte[4096];
							int count;
							try
							{
								while( (count = in.read( buf, 0, 4096 )) != -1 )
									out.write( buf, 0, count );
							}
							catch( IOException e )
							{
								// Do nothing. Stream is probably already closed.
							}
						}
					} ).start();

			/*
			 * Thread for reading stderr.
			 */
			final StringBuilder stderrMessage = new StringBuilder();
			final BufferedReader stderr = new BufferedReader( new InputStreamReader( proc.getErrorStream() ) );
			new Thread( new Runnable()
			{

				public void run()
				{
					String in;
					try
					{
						while( (in = stderr.readLine()) != null )
							stderrMessage.append( in );
					}
					catch( IOException e )
					{
						// Do nothing. Stream is probably already closed.
					}
				}
			} ).start();

			proc.waitFor();

			if( stderrMessage.length() != 0 )
				GitVcs.getInstance( project ).showMessages( "ERROR: " + stderrMessage.toString() );

			ByteArrayInputStream result = new ByteArrayInputStream( out.toByteArray() );

			// Clean up.
			in.close();
			out.close();
			stderr.close();
			return result;
		}
		catch( InterruptedException e )
		{
			throw new VcsException( e );
		}
		catch( IOException e )
		{
			throw new VcsException( e );
		}
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
	private String getBasePath()
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

		execute( "commit", options, args );
	}

	public void delete( VirtualFile[] files ) throws VcsException
	{
		String baseDirStr = getBasePath();
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

		execute( DELETE_CMD, (String[]) null, fixedFileNames );
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

		String result = convertStreamToString( execute( "log", options, args ) );
		GitVcs.getInstance( project ).showMessages( result );


        return gitLogParser.getRevisionsFrom(filePath, result, project);
	}

    public String version() throws VcsException
	{
		return convertStreamToString( execute( "version" ) );
	}

	public String tag( String tagName ) throws VcsException
	{
		return convertStreamToString( execute( "tag", tagName ) );
	}

	public void pull( String respository, boolean update ) throws VcsException
	{
		String options = null;
		if( update )
			options = "-u";

		String result = convertStreamToString( execute( "pull", options, respository ) );
		GitVcs.getInstance( project ).showMessages( result );
	}

	public void merge() throws VcsException
	{
		execute( "merge" );
	}

	public void push( String repository ) throws VcsException
	{
		String result = convertStreamToString( execute( "push", repository ) );
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

		execute( "clone", (String) null, args );
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
		execute( "rename", (String[]) null, args );
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
		execute( "rename", (String[]) null, args );
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
		execute( "copy", (String[]) null, args );
	}

}
