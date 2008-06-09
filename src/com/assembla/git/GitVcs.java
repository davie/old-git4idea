package com.assembla.git;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.ChangeProvider;
import com.intellij.openapi.vcs.checkin.CheckinEnvironment;
import com.intellij.openapi.vcs.diff.DiffProvider;
import com.intellij.openapi.vcs.history.VcsHistoryProvider;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class GitVcs extends AbstractVcs implements Disposable
{
	private static final Logger LOG = Logger.getInstance( "com.assembla.git.GitVcs" );

	private GitChangeProvider changeProvider;
	private VcsShowConfirmationOption addConfirmation;
	private VcsShowConfirmationOption delConfirmation;

	private GitCheckinEnvironment checkinEnvironment;

	private GitDiffProvider diffProvider;
	private GitHistoryProvider historyProvider;

	private Disposable activationDisposable;
	private final ProjectLevelVcsManager vcsManager;
	private final GitVcsSettings settings;
	private EditorColorsScheme editorColorsScheme;
	private Configurable myConfigurable;

	public GitVcs(
			@NotNull Project myProject,
			@NotNull final GitChangeProvider gitChangeProvider,
			@NotNull final GitCheckinEnvironment gitCheckinEnvironment,
			@NotNull final ProjectLevelVcsManager vcsManager,
			@NotNull final GitDiffProvider diffProvider,
			@NotNull final GitHistoryProvider historyProvider,
			@NotNull final GitVcsSettings settings )
	{
		super( myProject );
		this.vcsManager = vcsManager;
		this.settings = settings;

		addConfirmation = vcsManager.getStandardConfirmation( VcsConfiguration.StandardConfirmation.ADD, this );
		delConfirmation = vcsManager.getStandardConfirmation( VcsConfiguration.StandardConfirmation.REMOVE, this );

		changeProvider = gitChangeProvider;
		checkinEnvironment = gitCheckinEnvironment;
		this.diffProvider = diffProvider;
		editorColorsScheme = EditorColorsManager.getInstance().getGlobalScheme();
		this.historyProvider = historyProvider;

	}

	@Override
	public void shutdown() throws VcsException
	{
		super.shutdown();
		dispose();
	}


	public void activate()
	{
		super.activate();
		activationDisposable = new Disposable()
		{
			public void dispose()
			{
			}
		};

		VirtualFileManager.getInstance().addVirtualFileListener(
				new GitVirtualFileAdaptor( this, myProject ),
				activationDisposable );
		GitFileSystemListener gitListener = new GitFileSystemListener( this, myProject );
		LocalFileSystem.getInstance().registerAuxiliaryFileOperationsHandler( gitListener );
		CommandProcessor.getInstance().addCommandListener( gitListener );
	}

	public void deactivate()
	{
		super.deactivate();

		assert activationDisposable != null;
		Disposer.dispose( activationDisposable );
		activationDisposable = null;
	}

	public VcsShowConfirmationOption getAddConfirmation()
	{
		return addConfirmation;
	}

	public VcsShowConfirmationOption getDeleteConfirmation()
	{
		return delConfirmation;
	}


	@NonNls
	public String getName()
	{
		return "Git";
	}

	@NonNls
	public String getDisplayName()
	{
		return "Git";
	}

	public Configurable getConfigurable()
	{
		if( myConfigurable == null )
		{
			myConfigurable = new GitVcsConfigurable( settings, myProject );
		}

		return myConfigurable;
	}

	@Override
	public boolean fileExistsInVcs( FilePath filePath )
	{
		FileStatus status = FileStatusManager.getInstance( myProject ).getStatus( filePath.getVirtualFile() );
		return !(status == FileStatus.UNKNOWN || status == FileStatus.ADDED);
	}

	@Override
	public boolean fileIsUnderVcs( FilePath filePath )
	{
		final VirtualFile parent = filePath.getVirtualFileParent();
		return parent != null && isVersionedDirectory( parent );
	}

	public boolean isVersionedDirectory( VirtualFile dir )
	{
		final VirtualFile versionFile = dir.findChild( ".hg" );
		return versionFile != null && versionFile.isDirectory();

	}

	public static GitVcs getInstance( Project project )
	{
		return (GitVcs) ProjectLevelVcsManager.getInstance( project ).findVcsByName( "Git" );
	}

	@Nullable
	public ChangeProvider getChangeProvider()
	{
		return changeProvider;
	}

	@Nullable
	public CheckinEnvironment getCheckinEnvironment()
	{
		return checkinEnvironment;
	}

	public void showErrors( java.util.List<VcsException> list, String action )
	{
		if( list.size() > 0 )
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append( "\n" );
			buffer.append( action ).append( " Error: " );
			VcsException e;
			for( Iterator<VcsException> iterator = list.iterator(); iterator.hasNext(); buffer.append( e.getMessage() ) )
			{
				e = iterator.next();
				buffer.append( "\n" );
			}
			showMessage( buffer.toString(), CodeInsightColors.ERRORS_ATTRIBUTES );
		}
	}

	public void showMessages( String message )
	{
		if( message == null )
			return;
		showMessage( message, HighlighterColors.TEXT );
	}

	private void showMessage( String message, final TextAttributesKey text )
	{
		vcsManager.addMessageToConsoleWindow( message, editorColorsScheme.getAttributes( text ) );
	}

	@Nullable
	public DiffProvider getDiffProvider()
	{
		return diffProvider;
	}

	public void dispose()
	{
		assert activationDisposable == null;
	}

	@Nullable
	public VcsHistoryProvider getVcsHistoryProvider()
	{
		return historyProvider;
	}

	public GitVcsSettings getSettings()
	{
		return settings;
	}
}
