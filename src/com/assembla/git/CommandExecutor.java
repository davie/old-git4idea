package com.assembla.git;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.EnvironmentUtil;

import java.io.*;
import java.util.*;

public class CommandExecutor {
    private final String gitExecutable;
    private IErrorHandler errorHandler;
    private File vcsRootDirectory;

    public CommandExecutor(String gitExecutable, IErrorHandler errorHandler, File vcsRootDirectory) {
        this.gitExecutable = gitExecutable;
        this.errorHandler = errorHandler;
        this.vcsRootDirectory = vcsRootDirectory;

    }

    public InputStream execute( String cmd, String arg ) throws VcsException
    {
        return execute( cmd, null, arg );
    }

    public InputStream execute( String cmd, String oneOption, String... args ) throws VcsException
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

    public InputStream execute( String cmd, String[] options, String[] args ) throws VcsException
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

    public InputStream execute( String cmd ) throws VcsException
    {
        return execute( cmd, Collections.<String>emptyList() );
    }

    public InputStream execute( String cmd, List<String> cmdArgs ) throws VcsException
    {
        /*
         * First, we build the proper command line. Then we execute it.
         */

//        PathManager.getPluginsPath();


        List<String> cmdLine = new ArrayList<String>();

        String[] execCmds = gitExecutable.split( " " );
        cmdLine.addAll(Arrays.asList(execCmds));

        cmdLine.add( cmd );
        cmdLine.addAll( cmdArgs );

        String cmdString = StringUtil.join( cmdLine, " " );
        errorHandler.displayErrorMessage("CMD: " + cmdString);

        /*
         * We now have the command line, so we execute it.
         */

        try
        {
            // FIXME remove intellij specifics
            final Map<String, String> environment = EnvironmentUtil.getEnviromentProperties();

            ProcessBuilder builder = new ProcessBuilder( cmdLine );
            builder.directory(vcsRootDirectory);

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

            if( stderrMessage.length() != 0 ) {
                String message = "ERROR: " + stderrMessage.toString();
                errorHandler.displayErrorMessage(message);
            }

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

}
